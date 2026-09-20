const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export type Book = {
  id: number;
  title: string;
  author: string;
  isbn: string;
  price: number;
  stockQuantity: number;
  createdAt: string;
  updatedAt: string;
};

export type BookPayload = {
  title: string;
  author: string;
  isbn: string;
  price: number;
  stockQuantity: number;
};

export type Customer = {
  id: number;
  name: string;
  email: string;
  phone?: string;
  createdAt: string;
  updatedAt: string;
};

export type CustomerPayload = {
  name: string;
  email: string;
  phone: string;
};

export type CheckoutItemPayload = {
  bookId: number;
  quantity: number;
};

export type Order = {
  id: number;
  customerId: number;
  customerName: string;
  totalAmount: number;
  createdAt: string;
  items: OrderItem[];
};

export type OrderItem = {
  bookId: number;
  title: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
};

type ApiError = {
  message?: string;
  details?: string[];
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...init?.headers
    },
    ...init
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const error = (await response.json()) as ApiError;
      const details = error.details?.join(" ");
      message = [error.message, details].filter(Boolean).join(" ");
    } catch {
      // Keep the fallback message when the response body is not JSON.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  getBooks: () => request<Book[]>("/api/books"),
  createBook: (payload: BookPayload) =>
    request<Book>("/api/books", { method: "POST", body: JSON.stringify(payload) }),
  updateBook: (id: number, payload: BookPayload) =>
    request<Book>(`/api/books/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteBook: (id: number) => request<void>(`/api/books/${id}`, { method: "DELETE" }),

  getCustomers: () => request<Customer[]>("/api/customers"),
  createCustomer: (payload: CustomerPayload) =>
    request<Customer>("/api/customers", { method: "POST", body: JSON.stringify(payload) }),
  updateCustomer: (id: number, payload: CustomerPayload) =>
    request<Customer>(`/api/customers/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteCustomer: (id: number) => request<void>(`/api/customers/${id}`, { method: "DELETE" }),

  getOrders: () => request<Order[]>("/api/orders"),
  checkout: (customerId: number, items: CheckoutItemPayload[]) =>
    request<Order>("/api/orders/checkout", {
      method: "POST",
      body: JSON.stringify({ customerId, items })
    })
};
