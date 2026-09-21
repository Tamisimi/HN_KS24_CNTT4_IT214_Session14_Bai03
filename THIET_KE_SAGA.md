# Thiết kế Choreography Saga

## a) Input / Output

### Input (bắt đầu saga)
| Trường | Mô tả |
|--------|------|
| customerId | Khách hàng |
| amount | Số tiền |
| walletId | Ví thanh toán |
| productId / quantity | Tồn kho (nếu có Inventory) |
| shippingAddress | Địa chỉ giao |

### Output (trạng thái cuối)
| Service | Thành công | Shipping fail / timeout |
|---------|------------|-------------------------|
| Order | COMPLETED | CANCELED |
| Payment | CHARGED | REFUNDED |
| Shipping | CREATED | FAILED (không tạo vận đơn) |

## b) Luồng thành công

```text
Order                Payment              Shipping
  |                     |                     |
  |-- OrderCreated ---->|                     |
  |   (PENDING)         |-- trừ tiền          |
  |                     |-- PaymentSuccess -->| 
  |                     |                     |-- kiểm tra địa chỉ
  |                     |                     |-- tạo vận đơn
  |<------------- ShippingSuccess ------------|
  | COMPLETED           |                     |
```

## c) Luồng bù trừ (ShippingFailed)

```text
Shipping -- ShippingFailed --> Order
Order    -- CompensatePayment --> Payment
Payment  -- refund --> RefundSuccess --> Order
Order    -- CANCELED
```

## d) Timeout Shipping 30 giây

- Order sau khi nhận PaymentSuccess: start timer / deadline = now + 30s.
- Nếu không có ShippingSuccess | ShippingFailed trước hạn:
  - Coi như **ShippingFailed**
  - Gửi **CompensatePayment** (cùng luồng bù trừ).

## e) Flowchart tổng

```text
[Start: Tạo Order PENDING]
        |
        v
[Publish OrderCreated]
        |
        v
[Payment: trừ tiền]
   |thành công          |lỗi
   v                    v
[PaymentSuccess]   [PaymentFailed → Order FAILED/CANCELED]
   |
   v
[Shipping: kiểm tra địa chỉ + tạo VĐ]
   |OK                    |fail / timeout 30s
   v                      v
[ShippingSuccess]   [ShippingFailed]
   |                      |
   v                      v
[Order COMPLETED]   [CompensatePayment]
                          |
                          v
                    [Payment Refund]
                          |
                          v
                    [RefundSuccess]
                          |
                          v
                    [Order CANCELED]
```

## Sự kiện (topic / event name)

| Event | Producer | Consumer |
|-------|----------|----------|
| OrderCreated | Order | Payment |
| PaymentSuccess | Payment | Shipping, Order |
| PaymentFailed | Payment | Order |
| ShippingSuccess | Shipping | Order |
| ShippingFailed | Shipping | Order |
| CompensatePayment | Order | Payment |
| RefundSuccess | Payment | Order |
