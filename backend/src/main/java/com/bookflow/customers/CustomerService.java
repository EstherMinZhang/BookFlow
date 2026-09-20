package com.bookflow.customers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookflow.common.ConflictException;
import com.bookflow.common.ResourceNotFoundException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return new CustomerResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("A customer with email " + request.getEmail() + " already exists.");
        }

        Customer customer = new Customer(request.getName(), request.getEmail(), request.getPhone());
        return new CustomerResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        customerRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("A customer with email " + request.getEmail() + " already exists.");
                });

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        return new CustomerResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
    }

    public Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " was not found."));
    }
}
