package com.pahana.bookshop.controller;

import java.io.*;
import java.math.BigDecimal;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.dao.ProductDAO;
import com.pahana.bookshop.dao.BookDAO;
import com.pahana.bookshop.dao.StationaryDAO;
import com.pahana.bookshop.model.Product;
import com.pahana.bookshop.model.Book;
import com.pahana.bookshop.model.Stationary;
import com.pahana.bookshop.factory.ProductFactory;
import com.pahana.bookshop.util.JsonUtil;
import java.util.List;
import java.util.Map;

@WebServlet(name = "productAPI", urlPatterns = {"/api/products/*"})
public class ProductAPI extends HttpServlet {
    private ProductDAO productDAO;
    private BookDAO bookDAO;
    private StationaryDAO stationaryDAO;

    public void init() {
        productDAO = new ProductDAO();
        bookDAO = new BookDAO();
        stationaryDAO = new StationaryDAO();
    }

    // GET /api/products - Get all products
    // GET /api/products/{id} - Get product by ID
    // GET /api/products/category/{category} - Get products by category
    // GET /api/products/search?name={name} - Search products by name
    // GET /api/products/books - Get all books
    // GET /api/products/books/genre/{genre} - Get books by genre
    // GET /api/products/books/author/{author} - Get books by author
    // GET /api/products/stationary - Get all stationary
    // GET /api/products/stationary/type/{type} - Get stationary by type
    // GET /api/products/stationary/brand/{brand} - Get stationary by brand
    // GET /api/products/lowstock?threshold={number} - Get low stock products
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String searchName = request.getParameter("name");
            String lowStockParam = request.getParameter("threshold");
            
            if (searchName != null) {
                // Search products by name
                List<Product> products = productDAO.searchByName(searchName);
                out.print(formatProductsListJson(products));
            } else if (lowStockParam != null) {
                // Get low stock products
                int threshold = Integer.parseInt(lowStockParam);
                List<Product> products = productDAO.findLowStock(threshold);
                out.print(formatProductsListJson(products));
            } else if (pathInfo == null || pathInfo.equals("/")) {
                // Get all products
                List<Product> products = productDAO.findAll();
                out.print(formatProductsListJson(products));
            } else {
                handleGetByPath(pathInfo, response, out);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }
    
    private void handleGetByPath(String pathInfo, HttpServletResponse response, PrintWriter out) throws Exception {
        String[] pathParts = pathInfo.split("/");
        
        if (pathParts.length == 2) {
            // GET /api/products/{id} or /api/products/{category}
            try {
                int productId = Integer.parseInt(pathParts[1]);
                Product product = productDAO.findById(productId);
                if (product != null) {
                    if ("BOOK".equals(product.getCategory())) {
                        Book book = bookDAO.findById(productId);
                        out.print(bookToJson(book));
                    } else if ("STATIONARY".equals(product.getCategory())) {
                        Stationary stationary = stationaryDAO.findById(productId);
                        out.print(stationaryToJson(stationary));
                    } else {
                        out.print(productToJson(product));
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.createErrorJson("Product not found"));
                }
            } catch (NumberFormatException e) {
                // If not a number, treat as category
                String category = pathParts[1].toUpperCase();
                if ("BOOKS".equals(category)) {
                    List<Book> books = bookDAO.findAll();
                    out.print(formatBooksListJson(books));
                } else if ("STATIONARY".equals(category)) {
                    List<Stationary> stationaryList = stationaryDAO.findAll();
                    out.print(formatStationaryListJson(stationaryList));
                } else {
                    List<Product> products = productDAO.findByCategory(category);
                    out.print(formatProductsListJson(products));
                }
            }
        } else if (pathParts.length == 3) {
            handleSpecificQueries(pathParts, response, out);
        }
    }
    
    private void handleSpecificQueries(String[] pathParts, HttpServletResponse response, PrintWriter out) throws Exception {
        String category = pathParts[1];
        String filter = pathParts[2];
        
        if ("books".equals(category)) {
            if (filter.startsWith("genre/") && pathParts.length >= 4) {
                String genre = pathParts[3];
                List<Book> books = bookDAO.findByGenre(genre);
                out.print(formatBooksListJson(books));
            } else if (filter.startsWith("author/") && pathParts.length >= 4) {
                String author = pathParts[3];
                List<Book> books = bookDAO.findByAuthor(author);
                out.print(formatBooksListJson(books));
            }
        } else if ("stationary".equals(category)) {
            if (filter.startsWith("type/") && pathParts.length >= 4) {
                String type = pathParts[3];
                List<Stationary> stationaryList = stationaryDAO.findByType(type);
                out.print(formatStationaryListJson(stationaryList));
            } else if (filter.startsWith("brand/") && pathParts.length >= 4) {
                String brand = pathParts[3];
                List<Stationary> stationaryList = stationaryDAO.findByBrand(brand);
                out.print(formatStationaryListJson(stationaryList));
            }
        }
    }

    // POST /api/products/book - Create new book
    // POST /api/products/stationary - Create new stationary item
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if ("/book".equals(pathInfo)) {
                handleCreateBook(request, response, out);
            } else if ("/stationary".equals(pathInfo)) {
                handleCreateStationary(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Available endpoints: /book, /stationary"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }
    
    private void handleCreateBook(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        Map<String, String> bookData = JsonUtil.parseSimpleJson(request.getReader());
        
        String name = bookData.get("name");
        String priceStr = bookData.get("price");
        String quantityStr = bookData.get("quantity");
        String genre = bookData.get("genre");
        String author = bookData.get("author");
        String isbn = bookData.get("isbn");
        String publisher = bookData.get("publisher");
        String pagesStr = bookData.get("pages");
        
        if (name == null || priceStr == null || quantityStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("name, price, and quantity are required"));
            return;
        }
        
        Book book = ProductFactory.createBook(
            name,
            new BigDecimal(priceStr),
            Integer.parseInt(quantityStr),
            genre,
            author,
            isbn
        );
        
        if (publisher != null) book.setPublisher(publisher);
        if (pagesStr != null) book.setPages(Integer.parseInt(pagesStr));
        
        int newProductId = bookDAO.create(book);
        if (newProductId > 0) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\": \"Book created successfully\", \"productId\": " + newProductId + "}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Failed to create book"));
        }
    }
    
    private void handleCreateStationary(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws Exception {
        Map<String, String> stationaryData = JsonUtil.parseSimpleJson(request.getReader());
        
        String name = stationaryData.get("name");
        String priceStr = stationaryData.get("price");
        String quantityStr = stationaryData.get("quantity");
        String type = stationaryData.get("type");
        String brand = stationaryData.get("brand");
        String color = stationaryData.get("color");
        String size = stationaryData.get("size");
        String material = stationaryData.get("material");
        
        if (name == null || priceStr == null || quantityStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("name, price, and quantity are required"));
            return;
        }
        
        Stationary stationary = ProductFactory.createStationary(
            name,
            new BigDecimal(priceStr),
            Integer.parseInt(quantityStr),
            type,
            brand
        );
        
        if (color != null) stationary.setColor(color);
        if (size != null) stationary.setSize(size);
        if (material != null) stationary.setMaterial(material);
        
        int newProductId = stationaryDAO.create(stationary);
        if (newProductId > 0) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\": \"Stationary item created successfully\", \"productId\": " + newProductId + "}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.createErrorJson("Failed to create stationary item"));
        }
    }

    // PUT /api/products/{id}/quantity - Update product quantity
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            
            if (pathParts.length == 3 && "quantity".equals(pathParts[2])) {
                int productId = Integer.parseInt(pathParts[1]);
                
                Map<String, String> updateData = JsonUtil.parseSimpleJson(request.getReader());
                String quantityStr = updateData.get("quantity");
                
                if (quantityStr != null) {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (productDAO.updateQuantity(productId, newQuantity)) {
                        out.print(JsonUtil.createSuccessJson("Product quantity updated successfully"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(JsonUtil.createErrorJson("Product not found"));
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.createErrorJson("quantity is required"));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // DELETE /api/products/{id} - Delete product
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            String[] pathParts = pathInfo.split("/");
            
            if (pathParts.length == 2) {
                int productId = Integer.parseInt(pathParts[1]);
                
                if (productDAO.delete(productId)) {
                    out.print(JsonUtil.createSuccessJson("Product deleted successfully"));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.createErrorJson("Product not found"));
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson(e.getMessage()));
        }
    }

    // JSON formatting methods
    private String formatProductsListJson(List<Product> products) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) json.append(",");
            json.append(productToJson(products.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String formatBooksListJson(List<Book> books) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            if (i > 0) json.append(",");
            json.append(bookToJson(books.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String formatStationaryListJson(List<Stationary> stationaryList) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < stationaryList.size(); i++) {
            if (i > 0) json.append(",");
            json.append(stationaryToJson(stationaryList.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String productToJson(Product product) {
        return "{\"productId\": " + product.getProductId() + 
               ", \"productCode\": \"" + product.getProductCode() + "\"" +
               ", \"name\": \"" + product.getName() + "\"" +
               ", \"price\": " + product.getPrice() +
               ", \"quantity\": " + product.getQuantity() +
               ", \"category\": \"" + product.getCategory() + "\"" +
               ", \"status\": \"" + product.getStatus() + "\"" +
               ", \"isLowStock\": " + product.isLowStock() +
               ", \"isOutOfStock\": " + product.isOutOfStock() +
               ", \"createdAt\": \"" + product.getCreatedAt() + "\"}";
    }
    
    private String bookToJson(Book book) {
        return "{\"productId\": " + book.getProductId() + 
               ", \"productCode\": \"" + book.getProductCode() + "\"" +
               ", \"name\": \"" + book.getName() + "\"" +
               ", \"price\": " + book.getPrice() +
               ", \"quantity\": " + book.getQuantity() +
               ", \"category\": \"" + book.getCategory() + "\"" +
               ", \"status\": \"" + book.getStatus() + "\"" +
               ", \"genre\": \"" + book.getGenre() + "\"" +
               ", \"author\": \"" + book.getAuthor() + "\"" +
               ", \"isbn\": \"" + book.getIsbn() + "\"" +
               ", \"publisher\": \"" + book.getPublisher() + "\"" +
               ", \"pages\": " + book.getPages() +
               ", \"isLowStock\": " + book.isLowStock() +
               ", \"isOutOfStock\": " + book.isOutOfStock() +
               ", \"createdAt\": \"" + book.getCreatedAt() + "\"}";
    }
    
    private String stationaryToJson(Stationary stationary) {
        return "{\"productId\": " + stationary.getProductId() + 
               ", \"productCode\": \"" + stationary.getProductCode() + "\"" +
               ", \"name\": \"" + stationary.getName() + "\"" +
               ", \"price\": " + stationary.getPrice() +
               ", \"quantity\": " + stationary.getQuantity() +
               ", \"category\": \"" + stationary.getCategory() + "\"" +
               ", \"status\": \"" + stationary.getStatus() + "\"" +
               ", \"type\": \"" + stationary.getType() + "\"" +
               ", \"brand\": \"" + stationary.getBrand() + "\"" +
               ", \"color\": \"" + stationary.getColor() + "\"" +
               ", \"size\": \"" + stationary.getSize() + "\"" +
               ", \"material\": \"" + stationary.getMaterial() + "\"" +
               ", \"isLowStock\": " + stationary.isLowStock() +
               ", \"isOutOfStock\": " + stationary.isOutOfStock() +
               ", \"createdAt\": \"" + stationary.getCreatedAt() + "\"}";
    }

    // Handle OPTIONS preflight requests
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cookie");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void destroy() {}
}