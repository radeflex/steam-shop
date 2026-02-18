import { useEffect, useState } from "react";
import { toast } from "react-toastify";

import {
  getCart,
  updateQuantity,
  removeFromCart,
  purchaseCartViaBalance,
  purchaseCartViaCard
} from "../api/cart.api";

import CartCard from "../components/CartCard";

export default function CartPage() {
  const [items, setItems] = useState([]);

  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [buyLoading, setBuyLoading] = useState(false);

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    const res = await getCart();
    setItems(res.data.content);
  };

  const increase = async (item) => {
    await updateQuantity(item.id, item.quantity + 1);
    loadCart();
  };

  const decrease = async (item) => {
    if (item.quantity === 1) {
      await removeFromCart(item.id);
    } else {
      await updateQuantity(item.id, item.quantity - 1);
    }
    loadCart();
  };

  const remove = async (item) => {
    await removeFromCart(item.id);
    loadCart();
  };

  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  const handleCheckoutClick = () => {
    setShowPaymentModal(true);
  };

  const handlePurchase = async (type) => {
    if (buyLoading) return;

    try {
      setBuyLoading(true);

      const res =
        type === "BALANCE"
          ? await purchaseCartViaBalance()
          : await purchaseCartViaCard();

      setShowPaymentModal(false);

      if (res.data?.url) {
        window.location.href = res.data.url;
        return;
      }

      toast.success("✅ Покупка создана!");
      loadCart();
    } catch (err) {
      const status = err?.response?.status;
      const data = err?.response?.data;

      // обработка 400 с errors
      if (status === 400 && data?.errors) {
        const errors = Array.isArray(data.errors)
          ? data.errors
          : Object.values(data.errors).flat();

        if (errors.length > 0) {
          errors.forEach((e) => toast.error(`❌ ${e}`));
        } else {
          toast.error("❌ Ошибка валидации");
        }
        return;
      }

      console.error(err);
      toast.error("❌ Ошибка при покупке");
    } finally {
      setBuyLoading(false);
    }
  };

  return (
    <>
      <div className="container mt-4">
        <div className="row">
          {/* PRODUCTS */}
          <div className="col-md-8">
            <h3>Shopping Cart</h3>

            {items.length === 0 && (
              <p className="text-muted">Your cart is empty</p>
            )}

            {items.map((item) => (
              <CartCard
                key={item.productId}
                item={item}
                onPlus={() => increase(item)}
                onMinus={() => decrease(item)}
                onRemove={() => remove(item)}
              />
            ))}
          </div>

          {/* SUMMARY */}
          <div className="col-md-4">
            <div className="card sticky-top" style={{ top: 20, zIndex: 10 }}>
              <div className="card-body">
                <h5>Total</h5>
                <hr />
                <h4>{total} ₽</h4>

                <button
                  className="btn btn-success w-100 mt-3"
                  disabled={items.length === 0}
                  onClick={handleCheckoutClick}
                >
                  Checkout
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ===== MODAL ===== */}
      {showPaymentModal && (
        <div
          className="modal fade show"
          style={{ display: "block", background: "rgba(0,0,0,0.5)" }}
          tabIndex="-1"
          onClick={() => !buyLoading && setShowPaymentModal(false)}
        >
          <div
            className="modal-dialog modal-dialog-centered"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Выберите способ оплаты</h5>

                <button
                  type="button"
                  className="btn-close"
                  onClick={() => !buyLoading && setShowPaymentModal(false)}
                />
              </div>

              <div className="modal-body">
                <p className="mb-2">
                  Товаров в корзине: <strong>{items.length}</strong>
                </p>
                <p className="mb-0">
                  Сумма к оплате: <strong>{total} ₽</strong>
                </p>
              </div>

              <div className="modal-footer d-flex gap-2">
                <button
                  className="btn btn-success w-100"
                  disabled={buyLoading}
                  onClick={() => handlePurchase("BALANCE")}
                >
                  {buyLoading ? "Обработка..." : "💰 Оплатить балансом"}
                </button>

                <button
                  className="btn btn-primary w-100"
                  disabled={buyLoading}
                  onClick={() => handlePurchase("CARD")}
                >
                  {buyLoading ? "Обработка..." : "💳 Оплатить картой"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
