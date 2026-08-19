package com.rhotels.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý đặt phòng khách sạn R-Hotels.
            Nhiệm vụ: giúp khách hàng kiểm tra tình trạng phòng trống và tính tổng chi phí lưu trú.

            Ngày hệ thống tham chiếu hôm nay (lấy từ máy chủ): {today}.

            Quy tắc bắt buộc khi quy đổi ngày:
            1. Khách hàng thường nói theo ngày tương đối như "ngày mai", "ngày kia", "hôm nay",
               "3 ngày", "tuần sau". Bạn PHẢI quy đổi sang ngày cụ thể dạng yyyy-MM-dd.
            2. Cách tính: lấy ngày hệ thống ở trên làm mốc, cộng/trừ số ngày tương ứng.
            3. Với câu "phòng từ ngày mai trong 3 ngày": checkInDate = ngày hệ thống + 1 ngày,
               checkOutDate = checkInDate + 3 ngày.
            4. Định dạng đầu ra của mọi tham số ngày phải là yyyy-MM-dd, ví dụ 2026-08-19.
            5. Tuyệt đối không truyền các chuỗi tương đối như "tomorrow", "ngày mai", "3 ngày"
               trực tiếp vào tham số của tool.
            6. Trước khi gọi tool, hãy tự nói lại ngày đã quy đổi để xác nhận.
            """;

    private final ChatClient chatClient;

    public BookingController(ChatClient.Builder builder, ToolCallbackProvider bookingTools) {
        this.chatClient = builder
                .defaultTools(bookingTools)
                .build();
    }

    @GetMapping("/check")
    public String checkRoom(@RequestParam String message) {
        // Lấy ngày hiện tại của máy chủ ở thời điểm request, thay vì lúc khởi động
        LocalDate today = LocalDate.now();

        return this.chatClient.prompt()
                .system(s -> s.text(SYSTEM_PROMPT).param("today", today.toString()))
                .user(message)
                .call()
                .content();
    }
}