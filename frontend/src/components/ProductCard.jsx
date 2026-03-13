import { useState } from "react";
import { toast } from "react-toastify";

import { getImageUrl } from "../api/file.api";
import { purchaseViaCard, purchaseViaBalance } from "../api/cart.api";
import { useCart } from "../context/CartContext";

export default function ProductCard({ product }) {
  const { addOrIncrease, findByProductId } = useCart();

  const inCart = findByProductId(product.id);

  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [buyLoading, setBuyLoading] = useState(false);

  const handleBuyOneClick = () => {
    setShowPaymentModal(true);
  };

  const handlePurchase = async (type) => {
    if (buyLoading) return;

    try {
      setBuyLoading(true);

      const res =
        type === "BALANCE"
          ? await purchaseViaBalance(product.id)
          : await purchaseViaCard(product.id);

      setShowPaymentModal(false);
      if (res?.data?.url) {
        window.location.href = res.data.url;
      } else {
        toast.success("✅ Покупка создана!");
      }
    } catch (err) {
      const status = err?.response?.status;
      const data = err?.response?.data;
      if (status === 409 && data?.error) {
        toast.error(`❌ ${data.error}`);
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
      <div className="card h-100 shadow-sm">
        {product.previewUrl && (
          <img
            src={getImageUrl(product.previewUrl)}
            className="card-img-top"
            alt={product.title}
            style={{ objectFit: "cover", height: "180px" }}
          />
        )}

        <div className="card-body d-flex flex-column">
          <h5 className="card-title">{product.title}</h5>
          <p className="card-text fw-bold">{product.price} ₽</p>

          <div className="mt-auto d-grid gap-2">
            <button
              className={`btn w-100 ${inCart ? "btn-success" : "btn-primary"}`}
              onClick={() => addOrIncrease(product.id)}
            >
              {inCart ? "Add one more" : "Add to cart"}
            </button>

            <button
              className="btn btn-warning w-100"
              onClick={handleBuyOneClick}
            >
              Покупка в 1 клик
            </button>
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
                  <strong>{product.title}</strong>
                </p>
                <p className="mb-0">
                  Сумма к оплате: <strong>{product.price} ₽</strong>
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
