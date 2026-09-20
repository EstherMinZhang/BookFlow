import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  BookOpen,
  CheckCircle2,
  CircleDollarSign,
  Loader2,
  PackageCheck,
  Plus,
  RefreshCw,
  Save,
  ShoppingCart,
  Trash2,
  Users
} from "lucide-react";
import { api, Book, BookPayload, CheckoutItemPayload, Customer, CustomerPayload, Order } from "./api";

type View = "books" | "customers" | "checkout";

const emptyBook: BookPayload = {
  title: "",
  author: "",
  isbn: "",
  price: 0,
  stockQuantity: 0
};

const emptyCustomer: CustomerPayload = {
  name: "",
  email: "",
  phone: ""
};

export function App() {
  const [view, setView] = useState<View>("books");
  const [books, setBooks] = useState<Book[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [bookForm, setBookForm] = useState<BookPayload>(emptyBook);
  const [customerForm, setCustomerForm] = useState<CustomerPayload>(emptyCustomer);
  const [editingBookId, setEditingBookId] = useState<number | null>(null);
  const [editingCustomerId, setEditingCustomerId] = useState<number | null>(null);
  const [selectedCustomerId, setSelectedCustomerId] = useState("");
  const [cart, setCart] = useState<Record<number, number>>({});
  const [status, setStatus] = useState("Ready");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const cartItems = useMemo(() => {
    return Object.entries(cart)
      .map(([bookId, quantity]) => {
        const book = books.find((item) => item.id === Number(bookId));
        return book ? { book, quantity } : null;
      })
      .filter((item): item is { book: Book; quantity: number } => Boolean(item));
  }, [books, cart]);

  const cartTotal = cartItems.reduce((sum, item) => sum + Number(item.book.price) * item.quantity, 0);
  const lowStockCount = books.filter((book) => book.stockQuantity > 0 && book.stockQuantity <= 3).length;
  const outOfStockCount = books.filter((book) => book.stockQuantity === 0).length;

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const [bookData, customerData, orderData] = await Promise.all([
        api.getBooks(),
        api.getCustomers(),
        api.getOrders()
      ]);
      setBooks(bookData);
      setCustomers(customerData);
      setOrders(orderData);
      setStatus("Synced");
    } catch (err) {
      setError(readError(err));
      setStatus("API unavailable");
    } finally {
      setLoading(false);
    }
  }

  async function handleBookSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      if (editingBookId) {
        await api.updateBook(editingBookId, bookForm);
        setStatus("Book updated");
      } else {
        await api.createBook(bookForm);
        setStatus("Book added");
      }
      setBookForm(emptyBook);
      setEditingBookId(null);
      await loadData();
    } catch (err) {
      setError(readError(err));
    }
  }

  async function handleCustomerSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      if (editingCustomerId) {
        await api.updateCustomer(editingCustomerId, customerForm);
        setStatus("Customer updated");
      } else {
        await api.createCustomer(customerForm);
        setStatus("Customer added");
      }
      setCustomerForm(emptyCustomer);
      setEditingCustomerId(null);
      await loadData();
    } catch (err) {
      setError(readError(err));
    }
  }

  async function removeBook(id: number) {
    setError("");
    try {
      await api.deleteBook(id);
      setCart((current) => {
        const next = { ...current };
        delete next[id];
        return next;
      });
      setStatus("Book removed");
      await loadData();
    } catch (err) {
      setError(readError(err));
    }
  }

  async function removeCustomer(id: number) {
    setError("");
    try {
      await api.deleteCustomer(id);
      if (selectedCustomerId === String(id)) {
        setSelectedCustomerId("");
      }
      setStatus("Customer removed");
      await loadData();
    } catch (err) {
      setError(readError(err));
    }
  }

  async function submitCheckout() {
    setError("");
    if (!selectedCustomerId || cartItems.length === 0) {
      setError("Choose a customer and add at least one book.");
      return;
    }

    const payload: CheckoutItemPayload[] = cartItems.map((item) => ({
      bookId: item.book.id,
      quantity: item.quantity
    }));

    try {
      const order = await api.checkout(Number(selectedCustomerId), payload);
      setStatus(`Order #${order.id} created`);
      setCart({});
      await loadData();
    } catch (err) {
      setError(readError(err));
    }
  }

  function editBook(book: Book) {
    setBookForm({
      title: book.title,
      author: book.author,
      isbn: book.isbn,
      price: Number(book.price),
      stockQuantity: book.stockQuantity
    });
    setEditingBookId(book.id);
    setView("books");
  }

  function editCustomer(customer: Customer) {
    setCustomerForm({
      name: customer.name,
      email: customer.email,
      phone: customer.phone ?? ""
    });
    setEditingCustomerId(customer.id);
    setView("customers");
  }

  function setCartQuantity(bookId: number, quantity: number) {
    setCart((current) => {
      const next = { ...current };
      if (quantity <= 0) {
        delete next[bookId];
      } else {
        next[bookId] = quantity;
      }
      return next;
    });
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <BookOpen size={24} aria-hidden="true" />
          <div>
            <strong>BookFlow</strong>
            <span>Inventory Console</span>
          </div>
        </div>
        <nav className="nav-list" aria-label="Main navigation">
          <button className={view === "books" ? "active" : ""} onClick={() => setView("books")}>
            <PackageCheck size={18} aria-hidden="true" />
            Books
          </button>
          <button className={view === "customers" ? "active" : ""} onClick={() => setView("customers")}>
            <Users size={18} aria-hidden="true" />
            Customers
          </button>
          <button className={view === "checkout" ? "active" : ""} onClick={() => setView("checkout")}>
            <ShoppingCart size={18} aria-hidden="true" />
            Checkout
          </button>
        </nav>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <h1>{viewTitle(view)}</h1>
            <p>{books.length} books · {customers.length} customers · {orders.length} orders</p>
          </div>
          <div className="topbar-actions">
            <span className="status-pill">{loading ? "Loading" : status}</span>
            <button className="icon-button" title="Refresh data" aria-label="Refresh data" onClick={() => void loadData()}>
              {loading ? <Loader2 className="spin" size={18} /> : <RefreshCw size={18} />}
            </button>
          </div>
        </header>

        {error && <div className="alert">{error}</div>}

        <section className="metrics" aria-label="Inventory summary">
          <div>
            <span>Total stock</span>
            <strong>{books.reduce((sum, book) => sum + book.stockQuantity, 0)}</strong>
          </div>
          <div>
            <span>Low stock</span>
            <strong>{lowStockCount}</strong>
          </div>
          <div>
            <span>Out of stock</span>
            <strong>{outOfStockCount}</strong>
          </div>
          <div>
            <span>Revenue tracked</span>
            <strong>${orders.reduce((sum, order) => sum + Number(order.totalAmount), 0).toFixed(2)}</strong>
          </div>
        </section>

        {view === "books" && (
          <BooksView
            books={books}
            form={bookForm}
            editingId={editingBookId}
            onFormChange={setBookForm}
            onSubmit={handleBookSubmit}
            onCancel={() => {
              setBookForm(emptyBook);
              setEditingBookId(null);
            }}
            onEdit={editBook}
            onDelete={removeBook}
          />
        )}

        {view === "customers" && (
          <CustomersView
            customers={customers}
            form={customerForm}
            editingId={editingCustomerId}
            onFormChange={setCustomerForm}
            onSubmit={handleCustomerSubmit}
            onCancel={() => {
              setCustomerForm(emptyCustomer);
              setEditingCustomerId(null);
            }}
            onEdit={editCustomer}
            onDelete={removeCustomer}
          />
        )}

        {view === "checkout" && (
          <CheckoutView
            books={books}
            customers={customers}
            cartItems={cartItems}
            selectedCustomerId={selectedCustomerId}
            total={cartTotal}
            recentOrders={orders.slice(-6).reverse()}
            onCustomerChange={setSelectedCustomerId}
            onQuantityChange={setCartQuantity}
            onCheckout={submitCheckout}
          />
        )}
      </main>
    </div>
  );
}

function BooksView(props: {
  books: Book[];
  form: BookPayload;
  editingId: number | null;
  onFormChange: (form: BookPayload) => void;
  onSubmit: (event: FormEvent) => void;
  onCancel: () => void;
  onEdit: (book: Book) => void;
  onDelete: (id: number) => void;
}) {
  return (
    <div className="content-grid">
      <form className="editor-panel" onSubmit={props.onSubmit}>
        <h2>{props.editingId ? "Edit book" : "Add book"}</h2>
        <label>
          Title
          <input value={props.form.title} onChange={(event) => props.onFormChange({ ...props.form, title: event.target.value })} required />
        </label>
        <label>
          Author
          <input value={props.form.author} onChange={(event) => props.onFormChange({ ...props.form, author: event.target.value })} required />
        </label>
        <label>
          ISBN
          <input value={props.form.isbn} onChange={(event) => props.onFormChange({ ...props.form, isbn: event.target.value })} required />
        </label>
        <div className="field-row">
          <label>
            Price
            <input type="number" min="0" step="0.01" value={props.form.price} onChange={(event) => props.onFormChange({ ...props.form, price: Number(event.target.value) })} required />
          </label>
          <label>
            Stock
            <input type="number" min="0" value={props.form.stockQuantity} onChange={(event) => props.onFormChange({ ...props.form, stockQuantity: Number(event.target.value) })} required />
          </label>
        </div>
        <div className="form-actions">
          <button type="submit">
            {props.editingId ? <Save size={16} /> : <Plus size={16} />}
            {props.editingId ? "Save" : "Add"}
          </button>
          {props.editingId && (
            <button type="button" className="secondary" onClick={props.onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <div className="table-panel">
        <div className="table-header">
          <h2>Inventory</h2>
          <span>{props.books.length} records</span>
        </div>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Author</th>
                <th>ISBN</th>
                <th>Price</th>
                <th>Stock</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {props.books.map((book) => (
                <tr key={book.id}>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td>{book.isbn}</td>
                  <td>${Number(book.price).toFixed(2)}</td>
                  <td><StockBadge value={book.stockQuantity} /></td>
                  <td className="row-actions">
                    <button className="text-button" onClick={() => props.onEdit(book)}>Edit</button>
                    <button className="icon-button danger" title="Delete book" aria-label={`Delete ${book.title}`} onClick={() => void props.onDelete(book.id)}>
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {props.books.length === 0 && <EmptyRow columns={6} text="No books yet" />}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function CustomersView(props: {
  customers: Customer[];
  form: CustomerPayload;
  editingId: number | null;
  onFormChange: (form: CustomerPayload) => void;
  onSubmit: (event: FormEvent) => void;
  onCancel: () => void;
  onEdit: (customer: Customer) => void;
  onDelete: (id: number) => void;
}) {
  return (
    <div className="content-grid">
      <form className="editor-panel" onSubmit={props.onSubmit}>
        <h2>{props.editingId ? "Edit customer" : "Add customer"}</h2>
        <label>
          Name
          <input value={props.form.name} onChange={(event) => props.onFormChange({ ...props.form, name: event.target.value })} required />
        </label>
        <label>
          Email
          <input type="email" value={props.form.email} onChange={(event) => props.onFormChange({ ...props.form, email: event.target.value })} required />
        </label>
        <label>
          Phone
          <input value={props.form.phone} onChange={(event) => props.onFormChange({ ...props.form, phone: event.target.value })} />
        </label>
        <div className="form-actions">
          <button type="submit">
            {props.editingId ? <Save size={16} /> : <Plus size={16} />}
            {props.editingId ? "Save" : "Add"}
          </button>
          {props.editingId && (
            <button type="button" className="secondary" onClick={props.onCancel}>
              Cancel
            </button>
          )}
        </div>
      </form>

      <div className="table-panel">
        <div className="table-header">
          <h2>Customers</h2>
          <span>{props.customers.length} records</span>
        </div>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {props.customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.name}</td>
                  <td>{customer.email}</td>
                  <td>{customer.phone || "-"}</td>
                  <td className="row-actions">
                    <button className="text-button" onClick={() => props.onEdit(customer)}>Edit</button>
                    <button className="icon-button danger" title="Delete customer" aria-label={`Delete ${customer.name}`} onClick={() => void props.onDelete(customer.id)}>
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
              {props.customers.length === 0 && <EmptyRow columns={4} text="No customers yet" />}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function CheckoutView(props: {
  books: Book[];
  customers: Customer[];
  cartItems: { book: Book; quantity: number }[];
  selectedCustomerId: string;
  total: number;
  recentOrders: Order[];
  onCustomerChange: (id: string) => void;
  onQuantityChange: (bookId: number, quantity: number) => void;
  onCheckout: () => void;
}) {
  return (
    <div className="checkout-layout">
      <section className="table-panel">
        <div className="table-header">
          <h2>Available books</h2>
          <span>{props.books.length} records</span>
        </div>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Book</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Qty</th>
              </tr>
            </thead>
            <tbody>
              {props.books.map((book) => (
                <tr key={book.id}>
                  <td>
                    <strong>{book.title}</strong>
                    <small>{book.author}</small>
                  </td>
                  <td>${Number(book.price).toFixed(2)}</td>
                  <td><StockBadge value={book.stockQuantity} /></td>
                  <td>
                    <input
                      className="quantity-input"
                      type="number"
                      min="0"
                      max={book.stockQuantity}
                      value={props.cartItems.find((item) => item.book.id === book.id)?.quantity ?? 0}
                      onChange={(event) => props.onQuantityChange(book.id, Number(event.target.value))}
                    />
                  </td>
                </tr>
              ))}
              {props.books.length === 0 && <EmptyRow columns={4} text="No books available" />}
            </tbody>
          </table>
        </div>
      </section>

      <aside className="checkout-panel">
        <h2>Checkout</h2>
        <label>
          Customer
          <select value={props.selectedCustomerId} onChange={(event) => props.onCustomerChange(event.target.value)}>
            <option value="">Select customer</option>
            {props.customers.map((customer) => (
              <option key={customer.id} value={customer.id}>{customer.name}</option>
            ))}
          </select>
        </label>

        <div className="cart-list">
          {props.cartItems.map((item) => (
            <div className="cart-line" key={item.book.id}>
              <span>{item.book.title}</span>
              <strong>{item.quantity} × ${Number(item.book.price).toFixed(2)}</strong>
            </div>
          ))}
          {props.cartItems.length === 0 && <p className="muted">Cart is empty.</p>}
        </div>

        <div className="checkout-total">
          <span>Total</span>
          <strong>${props.total.toFixed(2)}</strong>
        </div>

        <button className="checkout-button" onClick={() => void props.onCheckout()}>
          <CircleDollarSign size={18} />
          Create order
        </button>

        <div className="recent-orders">
          <h3>Recent orders</h3>
          {props.recentOrders.map((order) => (
            <div className="order-line" key={order.id}>
              <CheckCircle2 size={16} />
              <span>#{order.id} · {order.customerName}</span>
              <strong>${Number(order.totalAmount).toFixed(2)}</strong>
            </div>
          ))}
          {props.recentOrders.length === 0 && <p className="muted">No orders yet.</p>}
        </div>
      </aside>
    </div>
  );
}

function StockBadge({ value }: { value: number }) {
  const className = value === 0 ? "stock out" : value <= 3 ? "stock low" : "stock";
  return <span className={className}>{value}</span>;
}

function EmptyRow({ columns, text }: { columns: number; text: string }) {
  return (
    <tr>
      <td colSpan={columns} className="empty-row">{text}</td>
    </tr>
  );
}

function viewTitle(view: View) {
  if (view === "customers") return "Customers";
  if (view === "checkout") return "Checkout";
  return "Books";
}

function readError(error: unknown) {
  return error instanceof Error ? error.message : "Something went wrong.";
}
