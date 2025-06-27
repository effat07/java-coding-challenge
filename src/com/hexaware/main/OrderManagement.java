package com.hexaware.main;

import java.util.*;

import com.hexaware.dao.*;
import com.hexaware.entity.*;
import com.hexaware.exception.*;

public class OrderManagement {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        IOrderManagementRepository service = new OrderProcessor();

        while (true) {
            System.out.println("\n=== Order Management Menu ===");
            System.out.println("1. Create User");
            System.out.println("2. Create Product");
            System.out.println("3. Create Order");
            System.out.println("4. Cancel Order");
            System.out.println("5. Get All Products");
            System.out.println("6. Get Orders by User");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); 

            try {
                switch (choice) {

                    case 1:
                        System.out.print("Enter user ID: ");
                        int uid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter username: ");
                        String uname = sc.nextLine();
                        System.out.print("Enter password: ");
                        String pass = sc.nextLine();
                        System.out.print("Enter role (admin/user): ");
                        String role = sc.nextLine();

                        User user = new User(uid, uname, pass, role);
                        service.createUser(user);
                        break;

                    case 2:
                        System.out.print("Enter admin user ID: ");
                        int aid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter username: ");
                        String ausername = sc.nextLine();
                        System.out.print("Enter password: ");
                        String apass = sc.nextLine();
                        System.out.print("Enter role: ");
                        String arole = sc.nextLine();

                        User admin = new User(aid, ausername, apass, arole);
                        
                        System.out.print("Enter product ID: ");
                        int pid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter product name: ");
                        String pname = sc.nextLine();
                        System.out.print("Enter description: ");
                        String desc = sc.nextLine();
                        System.out.print("Enter price: ");
                        double price = sc.nextDouble();
                        System.out.print("Enter quantity: ");
                        int qty = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter type (electronics/clothing): ");
                        String type = sc.nextLine();

                        Product product;
                        if ("electronics".equalsIgnoreCase(type)) {
                            System.out.print("Enter brand: ");
                            String brand = sc.nextLine();
                            System.out.print("Enter warranty (months): ");
                            int warranty = sc.nextInt();
                            sc.nextLine();
                            product = new Electronics(pid, pname, desc, price, qty, type, brand, warranty);
                        } else if ("clothing".equalsIgnoreCase(type)) {
                            System.out.print("Enter size: ");
                            String size = sc.nextLine();
                            System.out.print("Enter color: ");
                            String color = sc.nextLine();
                            product = new Clothing(pid, pname, desc, price, qty, type, size, color);
                        } else {
                            System.out.println("Invalid product type.");
                            break;
                        }

                        service.createProduct(admin, product);
                        break;

                    case 3:
                        System.out.print("Enter user ID: ");
                        int ouid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter username: ");
                        String ouname = sc.nextLine();
                        System.out.print("Enter password: ");
                        String opass = sc.nextLine();
                        System.out.print("Enter role: ");
                        String orole = sc.nextLine();
                        User orderingUser = new User(ouid, ouname, opass, orole);

                        List<Product> orderProducts = new ArrayList<>();
                        while (true) {
                            System.out.print("Enter product ID to add to order (0 to stop): ");
                            int orderPid = sc.nextInt();
                            if (orderPid == 0) break;

                          
                            Product orderProduct = new Product();
                            orderProduct.setProductId(orderPid);
                            orderProducts.add(orderProduct);
                        }

                        service.createOrder(orderingUser, orderProducts);
                        break;

                    case 4:
                        System.out.print("Enter user ID: ");
                        int cancelUid = sc.nextInt();
                        System.out.print("Enter order ID to cancel: ");
                        int cancelOid = sc.nextInt();
                        service.cancelOrder(cancelUid, cancelOid);
                        break;

                    case 5:
                        List<Product> products = service.getAllProducts();
                        System.out.println("--- All Products ---");
                        for (Product p : products) {
                            System.out.println(p.getProductId() + " - " + p.getProductName() + " (" + p.getType() + ") - ₹" + p.getPrice());
                        }
                        break;

                    case 6:
                        System.out.print("Enter user ID: ");
                        int uoid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter username: ");
                        String uoname = sc.nextLine();
                        System.out.print("Enter password: ");
                        String uopass = sc.nextLine();
                        System.out.print("Enter role: ");
                        String uorole = sc.nextLine();

                        User orderUser = new User(uoid, uoname, uopass, uorole);
                        List<Order> userOrders = service.getOrderByUser(orderUser);
                        System.out.println("--- Orders for " + uoname + " ---");
                        for (Order o : userOrders) {
                            System.out.println("Order ID: " + o.getOrderId() + " | Date: " + o.getOrderDate());
                        }
                        break;

                    case 7:
                        System.out.println("Exiting");
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (UserNotFoundException | OrderNotFoundException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("EXCEPTION: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
