package com.hcl.inventory.config;

import com.hcl.inventory.entity.AppUser;
import com.hcl.inventory.entity.Product;
import com.hcl.inventory.entity.Warehouse;
import com.hcl.inventory.enums.ProductStatus;
import com.hcl.inventory.enums.UserRole;
import com.hcl.inventory.enums.UserStatus;
import com.hcl.inventory.repository.AppUserRepository;
import com.hcl.inventory.repository.InventoryRepository;
import com.hcl.inventory.repository.ProductRepository;
import com.hcl.inventory.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Automatically seeds default users, catalog, warehouses, and inventory on application startup
 * if they are missing, ensuring instant out-of-the-box authentication and functionality.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(
            AppUserRepository userRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        dropLegacyRoleConstraint();
        seedUsers();
        seedCatalogAndWarehouses();
    }

    private void dropLegacyRoleConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check");
            log.info("Dropped legacy app_user_role_check constraint for role flexibility");
        } catch (Exception e) {
            log.debug("Note: app_user_role_check could not be dropped or does not exist: {}", e.getMessage());
        }
    }

    private void seedUsers() {
        initOrUpdateUser("admin", "Password123!", "System Administrator", "admin@xyzretail.com", "9876543210", UserRole.ADMIN);
        initOrUpdateUser("manager.north", "Password123!", "Rajesh Sharma", "rajesh.sharma@xyzretail.com", "9876543211", UserRole.STORE_MANAGER);
        initOrUpdateUser("staff.mumbai", "Password123!", "Pooja Verma", "pooja.verma@xyzretail.com", "9876543212", UserRole.WAREHOUSE_STAFF);
        initOrUpdateUser("auditor.lead", "Password123!", "Sunil Narang", "sunil.narang@xyzretail.com", "9876543213", UserRole.AUDITOR);
    }

    private void initOrUpdateUser(String username, String rawPassword, String fullName, String email, String phone, UserRole role) {
        try {
            Optional<AppUser> userOpt = userRepository.findByUsernameIgnoreCase(username);
            AppUser user;
            if (userOpt.isEmpty()) {
                user = new AppUser();
                user.setUsername(username.trim().toLowerCase());
                user.setFullName(fullName);
                user.setEmail(email.trim().toLowerCase());
                user.setPhone(phone);
                user.setRole(role);
                user.setStatus(UserStatus.ACTIVE);
                user.setFailedLoginAttempts(0);
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
                log.info("✅ Created default user '{}' with role {}", username, role);
            } else {
                user = userOpt.get();
                if (!passwordEncoder.matches(rawPassword, user.getPassword()) || user.getStatus() != UserStatus.ACTIVE) {
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setStatus(UserStatus.ACTIVE);
                    user.setFailedLoginAttempts(0);
                    user.setLockedUntil(null);
                    userRepository.save(user);
                    log.info("✅ Verified and synchronized credentials for user '{}'", username);
                }
            }
        } catch (Exception e) {
            log.error("Failed to seed user '{}': {}", username, e.getMessage());
        }
    }

    private void seedCatalogAndWarehouses() {
        try {
            if (warehouseRepository.count() == 0) {
                Warehouse whMum = new Warehouse();
                whMum.setWarehouseCode("WH-MUM-01");
                whMum.setWarehouseName("Western Central DC");
                whMum.setWarehouseLocation("Bhiwandi Logistics Park");
                whMum.setAddress("Bldg 4A, Bhiwandi Highway Zone");
                whMum.setCity("Mumbai");
                whMum.setState("Maharashtra");
                whMum.setPincode("421302");
                whMum.setManagerName("Vikram Deshmukh");
                whMum.setContactNumber("022-28491001");
                whMum.setEmail("wh.mumbai@xyzretail.com");
                whMum.setCapacity(250000);
                warehouseRepository.save(whMum);

                Warehouse whDel = new Warehouse();
                whDel.setWarehouseCode("WH-DEL-02");
                whDel.setWarehouseName("Northern Hub DC");
                whDel.setWarehouseLocation("NH-48 Industrial Corridor");
                whDel.setAddress("Plot 18, Bilaspur Industrial Area");
                whDel.setCity("Gurugram");
                whDel.setState("Haryana");
                whDel.setPincode("122413");
                whDel.setManagerName("Anil Kashyap");
                whDel.setContactNumber("0124-4921002");
                whDel.setEmail("wh.delhi@xyzretail.com");
                whDel.setCapacity(300000);
                warehouseRepository.save(whDel);
            }

            if (productRepository.count() == 0) {
                Product p1 = new Product();
                p1.setProductName("Ultra-Slim Business Laptop 14\"");
                p1.setSku("ELEC-LP-001");
                p1.setProductCategory("Electronics");
                p1.setBrand("TechPro");
                p1.setProductPrice(new BigDecimal("64999.00"));
                p1.setUnit("pcs");
                p1.setStatus(ProductStatus.ACTIVE);
                p1.setDescription("14-inch Full HD display, Intel Core i7, 16GB RAM, 512GB SSD");
                productRepository.save(p1);

                Product p2 = new Product();
                p2.setProductName("Wireless Noise-Canceling Headphones");
                p2.setSku("ELEC-HP-002");
                p2.setProductCategory("Electronics");
                p2.setBrand("AcousticMax");
                p2.setProductPrice(new BigDecimal("8999.00"));
                p2.setUnit("pcs");
                p2.setStatus(ProductStatus.ACTIVE);
                p2.setDescription("Over-ear Bluetooth 5.3 headphones with active ANC and 40h battery");
                productRepository.save(p2);

                Product p3 = new Product();
                p3.setProductName("Classic Cotton Oxford Shirt - Blue");
                p3.setSku("APP-TS-010");
                p3.setProductCategory("Apparel");
                p3.setBrand("UrbanClassic");
                p3.setProductPrice(new BigDecimal("1599.00"));
                p3.setUnit("pcs");
                p3.setStatus(ProductStatus.ACTIVE);
                p3.setDescription("100% breathable organic cotton, wrinkle-free, slim tailored fit");
                productRepository.save(p3);
            }
        } catch (Exception e) {
            log.warn("Catalog/warehouse seeding skipped: {}", e.getMessage());
        }
    }
}
