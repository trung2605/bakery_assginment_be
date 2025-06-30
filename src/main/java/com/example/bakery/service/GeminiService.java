package com.example.bakery.service;

import com.example.bakery.dto.gemini.ChatbotMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${google.gemini.model-name}")
    private String modelName;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String sendMessageToGemini(String userMessage, List<ChatbotMessage> chatHistory) throws IOException {
        List<ChatbotMessage> currentChatHistory = (chatHistory != null) ? chatHistory : new ArrayList<>();

        log.info("Sending message to Gemini. User: '{}', History size: {}", userMessage, currentChatHistory.size());

        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsArray = requestBody.putArray("contents");

        // --- Thêm System Instruction ---
        ObjectNode systemInstructionUserPart = contentsArray.addObject();
        systemInstructionUserPart.put("role", "user");
        ArrayNode systemPartsArray = systemInstructionUserPart.putArray("parts");
        systemPartsArray.addObject().put("text", "Bạn là một trợ lý ảo của Dola Bakery, chuyên về các loại bánh ngọt, bánh mì, và đồ uống. " +
                "Hãy trả lời các câu hỏi về sản phẩm, chương trình khuyến mãi, giờ mở cửa, địa chỉ, hoặc hỗ trợ đặt hàng. " +
                "Luôn giữ thái độ thân thiện, nhiệt tình và chuyên nghiệp. Nếu câu hỏi nằm ngoài phạm vi, " +
                "\"**Quan trọng:** Luôn định dạng các danh sách sản phẩm hoặc thông tin chi tiết bằng cách sử dụng **Markdown**, \" +\n" +
                "                                            \"cụ thể là: **heading đậm** cho các loại chính, và xuống dòng với dấu đầu dòng (*) cho các mục con." +
                "hãy lịch sự thông báo và đề nghị hỗ trợ các chủ đề liên quan đến bánh ngọt. với phần database của chi nhánh và sản phầm được viết dưới đây:" +
                "\t-- Create table Products\n" +
                "\tCREATE TABLE products (\n" +
                "\t\tproduct_id CHAR(7) PRIMARY KEY CHECK (product_id LIKE 'PRD____'),\n" +
                "\t\tname VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,\n" +
                "\t\tdescription TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,\n" +
                "\t\tprice DECIMAL(10,0) NOT NULL,\n" +
                "\t\tstock_quantity INT DEFAULT 0 CHECK (stock_quantity >= 0),\n" +
                "\t\tcategory VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                "\t\timage_url VARCHAR(255),\n" +
                "\t\texpiration_date VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci\n" +
                "\t) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n" +
                "\n" +
                "\t-- Create table Branches\n" +
                "\tCREATE TABLE branches (\n" +
                "\t\tid INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "\t\tname VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                "\t\taddress VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                "\t\thotline VARCHAR(20),\n" +
                "\t\tmap_url VARCHAR(1000)\n" +
                "\t) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;" +
                "INSERT INTO products (product_id, name, description, price, stock_quantity, image_url, category, expiration_date)\n" +
                "\tVALUES\n" +
                "\t('PRD0001', 'Bánh Chiffon Trà Xanh', 'Bánh Chiffon mềm mịn với hương vị trà xanh thơm ngon.', 36000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-26-bb4406d9.jpg?v=1692093366447', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0002', 'Bánh Chiffon 3 Vị', 'Bánh Chiffon kết hợp 3 hương vị độc đáo.', 22000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-29-546f7bfe.jpg?v=1692093302560', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0003', 'Bánh Mì Nhân Sen Sữa Dừa', 'Bánh mì tươi với nhân sen và sữa dừa béo ngậy.', 10000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/sen-sua-dua-ac9cc58a8ba545aaa03c.jpg?v=1692092922617', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0004', 'Bánh Mì Hạt Óc Chó Sốt Kem', 'Bánh mì giòn rụm với hạt óc chó và sốt kem béo.', 30000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-22-a231cf82.jpg?v=1692092861340', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0005', 'Bánh Donut Socola Dâu 45G', 'Bánh donut phủ socola và dâu ngọt ngào.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/20-f815cca7a6314f428132e9c6b0472.jpg?v=1692092825777', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0006', 'Bánh Donut Socola Trắng Hạnh Nhân 45G', 'Bánh donut socola trắng với hạnh nhân giòn tan.', 20000, 50, 'https://product.hstatic.net/200000411281/product/khong_co_tieu_de__728___90_px___600___600_px___1__41c78a732f54428580f34db8e2d26f94_master.png', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0007', 'Bánh Donut Socola Trà Xanh 45G', 'Bánh donut socola trà xanh thơm lừng.', 15000, 50, 'https://www.binrecipes.com/wp-content/uploads/2025/03/close-up-matcha-mochi-doughnut-glazed-768x538.webp', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0008', 'Bánh Mì Xúc Xích Ruốc', 'Bánh mì mềm với xúc xích và ruốc đậm đà.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-18-5dc8fb8e.jpg?v=1692092726203', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0009', 'Bánh Mì Nhân Sợi Gà Sốt Teriyaki', 'Bánh mì tươi với gà sốt teriyaki đậm vị.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/banh-tuoi-soi-ga-sot-teriyaki-1.jpg?v=16920926953670a975e6-min.png?v=1692091986087', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0010', 'Bánh Mousse Chocolate', 'Bánh mousse socola mềm mịn, tan chảy.', 390000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-chua-co-ten-a480f34a8c8.jpg?v=1692091849157', 'Bánh Kem', '7 Ngày'),\n" +
                "\t('PRD0011', 'Bánh Kem Sweet Heart 4', 'Bánh kem hình trái tim ngọt ngào cho các dịp đặc biệt.', 150000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/2-removebg-preview-1-ba51f7d4e.png?v=1692091666957', 'Bánh Kem', '7 Ngày'),\n" +
                "\t('PRD0012', 'Bánh Kem Amazing Chocolate', 'Bánh kem socola tuyệt hảo cho tín đồ ngọt.', 380000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/banh-kem-amazing-chocolate-1-c.png?v=1692091472963', 'Bánh Kem', '7 Ngày'),\n" +
                "\t('PRD0013', 'Bánh Kem Endless Love', 'Bánh kem tình yêu vĩnh cửu, ngọt ngào.', 380000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/endless-love-c7027cf9711b4fde9b6.png?v=1692091413697', 'Bánh Kem', '7 Ngày'),\n" +
                "\t('PRD0014', 'Bánh Kem Princess', 'Bánh kem công chúa xinh đẹp và tinh tế.', 380000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/princess-8-58d2fc00a1d24627a63e3.png?v=1692091370170', 'Bánh Kem', '7 Ngày'),\n" +
                "\t('PRD0015', 'Bánh Sừng Bò Mini', 'Bánh sừng bò mini giòn tan, thơm bơ.', 36000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/26-52b2528f0cb542bbbcb7810df8e62.jpg?v=1692095091097', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0016', 'Bánh Mì Nướng Phô Mai Que', 'Bánh mì nướng với phô mai que béo ngậy.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/3-fdd1265100174cc3a112fde516b520.jpg?v=1692095046793', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0017', 'Bánh Mì Nướng Caramen', 'Bánh mì nướng với lớp caramen ngọt ngào.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/2-862efa1275f7462c9e2680575e45da.jpg?v=1692094997170', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0018', 'Bánh Mì Nướng Bơ Tỏi', 'Bánh mì nướng thơm lừng bơ tỏi.', 15000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/9-1cc15e71b97d405bbef69f4c166c44.jpg?v=1692094932597', 'Bánh Mì', '3 Ngày'),\n" +
                "\t('PRD0019', 'Bánh Quy Dừa', 'Bánh quy giòn tan với hương dừa thơm ngon.', 42000, 20, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/quy-dua-to-nho-9719c816ba7247699.jpg?v=1692094860540', 'Bánh Khô', '30 Ngày'),\n" +
                "\t('PRD0020', 'Bánh Quy Hạnh Nhân', 'Bánh quy thơm lừng với hạnh nhân béo bùi.', 42000, 20, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/quy-hanh-nhan-d1ba3f7c70fb43d0b1.jpg?v=1692094804490', 'Bánh Khô', '30 Ngày'),\n" +
                "\t('PRD0021', 'Bánh Quy Bơ Mứt Dâu', 'Bánh quy bơ thơm kết hợp mứt dâu ngọt.', 42000, 20, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/quy-mut-dau-to-a0e6bb6421aa47bda.jpg?v=1692094744600', 'Bánh Khô', '30 Ngày'),\n" +
                "\t('PRD0022', 'Bánh Lady Finger', 'Bánh Lady Finger nhẹ nhàng, mềm mịn.', 42000, 20, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/lady-finger-6bd9609cce084948a182.jpg?v=1692094694990', 'Bánh Khô', '30 Ngày'),\n" +
                "\t('PRD0023', 'Bánh Pana Cotta', 'Bánh Pana Cotta mềm mịn, ngọt nhẹ.', 22000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/panna-cotta-3459143e9ffa4e01bcfe.jpg?v=1692094440360', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0024', 'Sữa Chua', 'Sữa chua mịn màng, chua ngọt tự nhiên.', 13000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/sua-chua-59d14133b74f49979da0edf.jpg?v=1692094394980', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0025', 'Caramen', 'Caramen ngọt ngào, tan chảy trong miệng.', 13000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/caramel-d7c7ad0a5a654cac8f76208d.jpg?v=1692094359980', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0026', 'Mousse Trà Xanh', 'Bánh mousse trà xanh thơm mát, mềm mịn.', 31000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-31-6c5684fa.jpg?v=1692094319223', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0027', 'Mousse Chanh Leo', 'Bánh mousse chanh leo chua ngọt hài hòa.', 31000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/54-472bcbe688e046dea6add446c0fe1.jpg?v=1692094268307', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0028', 'Bánh Red Velvet 90G', 'Bánh Red Velvet đỏ mịn với lớp kem béo.', 58000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-5-0619631ba.jpg?v=1692094227680', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0029', 'Bánh Tiramisu 90G', 'Bánh Tiramisu thơm cà phê và kem mascarpone.', 36000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/28-bd8429ed0aa94f4cb872f92dfa4b9.jpg?v=1692094191763', 'Bánh Tráng Miệng', '7 Ngày'),\n" +
                "\t('PRD0030', 'Bánh Opera 90G', 'Bánh Opera tầng lớp socola và cà phê tinh tế.', 36000, 10, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/27-5c9d407149054a0caab958d17fc7a.jpg?v=1692094146947', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0031', 'Bánh Su Kem Nhân Vani', 'Bánh su kem mềm với nhân vani ngọt ngào.', 29000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/49-2f4173248ab5495babe1c9ddc804c.jpg?v=1692093856747', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0032', 'Bánh Su Kem Nhân Socola', 'Bánh su kem thơm ngon với nhân socola đậm đà.', 30000, 50, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/48-ba0acd283d9944f5bfa21d4026b29.jpg?v=1692093815550', 'Bánh Ngọt', '3 Ngày'),\n" +
                "\t('PRD0033', 'Bánh Muffin Socola Chip', 'Bánh muffin mềm với socola chip tan chảy.', 19000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/17-0bab5559a3714b46b407517296419.jpg?v=1692093767223', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0034', 'Bánh Gato Socola Sữa', 'Bánh gato socola sữa mềm mịn, ngọt ngào.', 40000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/thiet-ke-khong-ten-2-8c5a3322f.jpg?v=1692093730967', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0035', 'Bánh Cuộn Socola Miếng', 'Bánh cuộn socola miếng nhỏ gọn, thơm ngon.', 23000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/83-5760a68327794f8c9675f73336293-min-1.jpg?v=1692093694480', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0036', 'Bánh Cuộn Vani 110G', 'Bánh cuộn vani mềm mịn, thơm béo.', 35000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/42-f9db27ab52dc45ddb159636c43e27.jpg?v=1692093486117', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0037', 'Bánh Cuộn Trà Xanh 110G', 'Bánh cuộn trà xanh thơm mát, ngọt nhẹ.', 35000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/43-38af50a70488465286ce3ff0040db.jpg?v=1692093451703', 'Bánh Ngọt', '7 Ngày'),\n" +
                "\t('PRD0038', 'Bánh Chiffon Vani', 'Bánh Chiffon vani mềm nhẹ, thơm lừng.', 32000, 30, 'https://bizweb.dktcdn.net/thumb/large/100/492/035/products/41-c43fc976b5554ed9a89c65d6f22db.jpg?v=1692093413140', 'Bánh Ngọt', '7 Ngày');\n" +
                "\n" +
                "\t-- Insert sample data for Branches\n" +
                "\tINSERT INTO branches (name, address, hotline, map_url) VALUES\n" +
                "\t('Cơ sở Ngũ Hành Sơn', '479 Mai Đăng Chơn, Hòa Quý, Ngũ Hành Sơn, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d30680.628041553206!2d108.22264276888052!3d16.009428295255674!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x314211001704cd0b%3A0x934aec1889fc089a!2zQW5oIFF1w6JuIEJha2VyeSAtIDU2NiBMw6ogVsSDbiBIaeG6v24!5e0!3m2!1svi!2s!4v1742734081760!5m2!1svi!2s'),\n" +
                "\t('Cơ sở Thanh Khê', '39 Lý Thái Tông, Thanh Khê Tây, Thanh Khê, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d30670.558674384425!2d108.13967764377593!3d16.074836384372976!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31421978d7d8c92d%3A0xc4fe23e8647ee642!2sAnh%20Qu%C3%A2n%20Bakery!5e0!3m2!1svi!2s!4v1742734182424!5m2!1svi!2s'),\n" +
                "\t('Cơ sở Liên Chiểu', '359 Nguyễn Lương Bằng, Hòa Khánh Bắc, Liên Chiểu, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d61340.236285054554!2d108.0569593287342!3d16.0776920531889!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31421fcc05edba31%3A0xc874dd685b2050c8!2sAnh%20Qu%C3%A2n%20Bakery!5e0!3m2!1svi!2s!4v1742734222533!5m2!1svi!2s'),\n" +
                "\t('Cơ sở Cẩm Lệ', '33 Trường Sơn, Hòa Thọ Tây, Cẩm Lệ, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d30675.554686245534!2d108.1631558689842!3d16.042415999303135!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31421916153e5631%3A0x3fd489e0f2825d6f!2sAnh%20Qu%C3%A2n%20Bakery!5e0!3m2!1svi!2s!4v1742734261627!5m2!1svi!2s'),\n" +
                "\t('Cơ sở Sơn Trà', '170 Phạm Cự Lượng, An Hải Bắc, Sơn Trà, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d15334.486773290053!2d108.22468757629397!3d16.08510938394776!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x314217909fb49021%3A0xc0fc6f6036ae4f54!2sAnh%20Qu%C3%A2n%20Bakery!5e0!3m2!1svi!2s!4v1742734341494!5m2!1svi!2s'),\n" +
                "\t('Cơ sở Hòa Vang', 'Đường ĐT 602, Hòa Sơn, Hòa Vang, Đà Nẵng.', '19001900', \n" +
                "\t'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d61340.236285054554!2d108.0569593287342!3d16.0776920531889!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31421fd674cafc63%3A0x7e9a593892edeaab!2zQW5oIFF1w6JuIEJha2VyeSAtIE5nw6MgQmEgSG_DoCBTxqFu!5e0!3m2!1svi!2s!4v1742734237715!5m2!1svi!2s');");




        ObjectNode systemInstructionModelPart = contentsArray.addObject();
        systemInstructionModelPart.put("role", "model");
        ArrayNode systemModelPartsArray = systemInstructionModelPart.putArray("parts");
        systemModelPartsArray.addObject().put("text", "Ok, tôi đã hiểu. Bây giờ tôi đã sẵn sàng để trò chuyện về bánh ngọt!");

        // --- Thêm lịch sử trò chuyện ---
        for (ChatbotMessage msg : currentChatHistory) {
            ObjectNode messageNode = contentsArray.addObject();

            // !!! SỬA LỖI Ở ĐÂY !!!
            // Ánh xạ vai trò để đảm bảo chỉ có "user" hoặc "model" được gửi đi
            String geminiRole = mapSenderToGeminiRole(msg.getSender());
            messageNode.put("role", geminiRole);

            ArrayNode partsArray = messageNode.putArray("parts");
            partsArray.addObject().put("text", msg.getText());
        }

        // --- Thêm tin nhắn hiện tại của người dùng ---
        ObjectNode currentUserMessage = contentsArray.addObject();
        currentUserMessage.put("role", "user"); // Luôn là "user" cho tin nhắn hiện tại
        ArrayNode currentUserParts = currentUserMessage.putArray("parts");
        currentUserParts.addObject().put("text", userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        return executeGeminiRequest(url, request, String.class, "candidates[0].content.parts[0].text");
    }

    /**
     * Helper method để ánh xạ các giá trị sender từ ChatbotMessage sang vai trò Gemini hợp lệ.
     * @param sender Giá trị sender từ ChatbotMessage (ví dụ: "User", "Bot", "Assistant").
     * @return Vai trò hợp lệ cho Gemini API ("user" hoặc "model").
     */
    private String mapSenderToGeminiRole(String sender) {
        if (sender == null) {
            return "user"; // Mặc định là user nếu sender là null
        }
        String lowerCaseSender = sender.toLowerCase();
        if ("user".equals(lowerCaseSender)) {
            return "user";
        } else if ("model".equals(lowerCaseSender) || "bot".equals(lowerCaseSender) || "assistant".equals(lowerCaseSender)) {
            return "model";
        } else {
            // Log cảnh báo nếu gặp vai trò không mong muốn
            log.warn("Unknown sender role '{}' received. Defaulting to 'user'.", sender);
            return "user"; // Mặc định là user cho các vai trò không xác định
        }
    }

    // Các phương thức executeGeminiRequest và parseJsonNodeByPath giữ nguyên
    private <T> T executeGeminiRequest(String url, HttpEntity<?> request, Class<T> responseType, String jsonPath) throws IOException {
        // ... (giữ nguyên code đã sửa trước đó)
        try {
            log.debug("Sending request to Gemini URL: {}", url);
            log.debug("Request Body: {}", request.getBody());

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Gemini raw response: {}", responseBody);
                if (jsonPath != null && !jsonPath.isEmpty()) {
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    String extractedText = parseJsonNodeByPath(rootNode, jsonPath);
                    if (extractedText != null) {
                        return (T) extractedText;
                    } else {
                        log.warn("Could not extract text from Gemini response using path: {}. Raw response: {}", jsonPath, responseBody);
                        return (T) "Xin lỗi, có vẻ tôi không thể tạo ra câu trả lời phù hợp lúc này.";
                    }
                } else {
                    return (T) responseBody;
                }
            } else {
                String errorMsg = String.format("Error calling Gemini API: %s - Status: %s", responseBody, response.getStatusCode());
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
        } catch (HttpClientErrorException e) {
            String errorResponse = e.getResponseBodyAsString();
            String errorMsg = String.format("Client error calling Gemini API: %s - Status: %s", errorResponse, e.getStatusCode());
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } catch (IOException e) {
            log.error("Error processing Gemini API response or IO issue: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new IOException("Lỗi không xác định khi gọi Gemini API: " + e.getMessage(), e);
        }
    }

    private String parseJsonNodeByPath(JsonNode rootNode, String path) {
        // ... (giữ nguyên code đã sửa trước đó)
        String[] parts = path.split("\\.");
        JsonNode currentNode = rootNode;
        for (String part : parts) {
            if (part.contains("[")) {
                String arrayName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));
                currentNode = currentNode.path(arrayName);
                if (currentNode.isArray() && currentNode.size() > index) {
                    currentNode = currentNode.get(index);
                } else {
                    return null;
                }
            } else {
                currentNode = currentNode.path(part);
            }
            if (currentNode.isMissingNode() || currentNode.isNull()) {
                return null;
            }
        }
        return currentNode.isTextual() ? currentNode.asText() : currentNode.toString();
    }
}