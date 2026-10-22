package com.cartnest.config;

import com.cartnest.model.Category;
import com.cartnest.model.Product;
import com.cartnest.model.User;
import com.cartnest.repository.CategoryRepository;
import com.cartnest.repository.ProductRepository;
import com.cartnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdmin();
        initializeCategories();
        initializeProducts();
        log.info("CartNest data initialization complete!");
    }

    private void initializeAdmin() {
        if (!userRepository.existsByEmail("admin@cartnest.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("CartNest")
                    .email("admin@cartnest.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phone("9999999999")
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin@cartnest.com / admin123");
        }
    }

    private void initializeCategories() {
        String[][] categories = {
            {"Electronics", "Latest gadgets and electronic devices", "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400"},
            {"Fashion", "Trendy clothing and accessories", "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=400"},
            {"Home & Kitchen", "Everything for your home", "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400"},
            {"Books", "Books across all genres", "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400"},
            {"Sports", "Sports and fitness equipment", "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=400"},
            {"Beauty", "Beauty and personal care products", "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400"}
        };

        for (String[] cat : categories) {
            if (!categoryRepository.existsByName(cat[0])) {
                categoryRepository.save(Category.builder()
                        .name(cat[0])
                        .description(cat[1])
                        .imageUrl(cat[2])
                        .build());
            }
        }
    }

    private void initializeProducts() {
        if (productRepository.countByActiveTrue() > 0) return;

        Category electronics = categoryRepository.findByName("Electronics").orElse(null);
        Category fashion = categoryRepository.findByName("Fashion").orElse(null);
        Category home = categoryRepository.findByName("Home & Kitchen").orElse(null);
        Category books = categoryRepository.findByName("Books").orElse(null);
        Category sports = categoryRepository.findByName("Sports").orElse(null);
        Category beauty = categoryRepository.findByName("Beauty").orElse(null);

        // Electronics
        saveProduct("Wireless Noise-Cancelling Headphones", "Premium over-ear headphones with 30hr battery and ANC", new BigDecimal("2999"), 50, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400", electronics, 4.5, 128);
        saveProduct("Smart Watch Series 5", "Health monitoring smartwatch with AMOLED display", new BigDecimal("4999"), 30, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400", electronics, 4.3, 89);
        saveProduct("Portable Bluetooth Speaker", "360° surround sound, waterproof, 20hr battery", new BigDecimal("1499"), 75, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400", electronics, 4.4, 201);
        saveProduct("4K Action Camera", "Waterproof 4K camera with image stabilization", new BigDecimal("6999"), 20, "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=400", electronics, 4.6, 67);

        // Fashion
        saveProduct("Men's Slim Fit Jeans", "Premium denim stretch jeans, available in multiple colors", new BigDecimal("999"), 100, "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", fashion, 4.2, 345);
        saveProduct("Women's Floral Summer Dress", "Light and breezy summer dress with floral print", new BigDecimal("799"), 80, "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400", fashion, 4.4, 178);
        saveProduct("Leather Sneakers", "Handcrafted premium leather sneakers", new BigDecimal("2499"), 45, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", fashion, 4.5, 256);

        // Home & Kitchen
        saveProduct("Stainless Steel Cookware Set", "10-piece non-stick stainless steel set", new BigDecimal("3499"), 25, "https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=400", home, 4.3, 92);
        saveProduct("Electric Coffee Maker", "12-cup programmable coffee maker with keep-warm plate", new BigDecimal("1999"), 40, "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400", home, 4.6, 134);
        saveProduct("Robot Vacuum Cleaner", "Smart mapping robot vacuum with auto-charging", new BigDecimal("12999"), 15, "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", home, 4.4, 56);

        // Books
        saveProduct("Atomic Habits", "An Easy & Proven Way to Build Good Habits & Break Bad Ones", new BigDecimal("349"), 200, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400", books, 4.8, 512);
        saveProduct("The Psychology of Money", "Timeless lessons on wealth, greed, and happiness", new BigDecimal("299"), 150, "https://images.unsplash.com/photo-1553729459-efe14ef6055d?w=400", books, 4.7, 398);

        // Sports
        saveProduct("Yoga Mat Premium", "6mm thick non-slip yoga mat with alignment lines", new BigDecimal("599"), 120, "https://images.unsplash.com/photo-1601925228073-1d8deaeb6d54?w=400", sports, 4.5, 289);
        saveProduct("Adjustable Dumbbell Set", "5-52.5 lbs adjustable dumbbells, replaces 15 sets", new BigDecimal("8999"), 10, "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400", sports, 4.7, 76);

        // Beauty
        saveProduct("Vitamin C Face Serum", "20% Vitamin C brightening serum with hyaluronic acid", new BigDecimal("699"), 90, "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab12?w=400", beauty, 4.3, 445);
        saveProduct("Professional Hair Dryer", "2000W ionic hair dryer with multiple heat settings", new BigDecimal("1299"), 60, "https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=400", beauty, 4.4, 167);

        log.info("Sample products created!");
    }

    private void saveProduct(String name, String desc, BigDecimal price, int stock,
                              String imageUrl, Category category, double rating, int reviews) {
        productRepository.save(Product.builder()
                .name(name)
                .description(desc)
                .price(price)
                .stockQuantity(stock)
                .imageUrl(imageUrl)
                .category(category)
                .active(true)
                .rating(rating)
                .reviewCount(reviews)
                .build());
    }
}
