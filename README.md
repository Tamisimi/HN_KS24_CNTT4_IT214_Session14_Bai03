# Bài 3 Session 14 — Choreography Saga (Order → Payment → Shipping)

## Tóm tắt

- **Choreography:** không có orchestrator trung tâm; mỗi service lắng nghe event và tự quyết định bước tiếp / bù trừ.
- **Happy path:** OrderCreated → PaymentSuccess → ShippingSuccess → Order COMPLETED.
- **Shipping fail:** ShippingFailed → CompensatePayment → RefundSuccess → Order CANCELED.
- **Timeout Shipping 30s:** Order job kích hoạt bù trừ như ShippingFailed.

Chi tiết I/O + flowchart: `THIET_KE_SAGA.md`
