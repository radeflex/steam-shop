import { useState } from "react";
import { useCart } from "../context/CartContext";
import { getImageUrl } from "../api/file.api";

export default function CartCard({ item, onPlus, onMinus, onRemove }) {
  const { refreshCart } = useCart();
  const [showModal, setShowModal] = useState(false);

  const handlePlus = async () => {
    if (onPlus) {
      await onPlus();
      await refreshCart();
    }
  };

  const handleMinus = async () => {
    if (onMinus) {
      await onMinus();
      await refreshCart();
    }
  };

  const handleRemove = async () => {
    if (onRemove) {
      await onRemove();
      await refreshCart();
      setShowModal(false);
    }
  };

  return (
    <>
      <div className="card mb-3">
        <div className="card-body d-flex align-items-center">
          
          {/* Левая часть: изображение и текст */}
          <div className="d-flex align-items-center flex-grow-1">
            {item.previewUrl && (
              <img
                src={getImageUrl(item.previewUrl)}
                alt={item.title}
                width="20%"
                className="me-3 rounded"
                style={{ objectFit: "cover", height: "80px" }}
              />
            )}
            <div className="text-truncate" style={{ maxWidth: "250px" }}>
              <h5 className="mb-1 text-truncate">{item.title}</h5>
              <div className="text-muted">{item.price} ₽ / шт</div>
            </div>
          </div>

          {/* Правая часть: кнопки и цена */}
          <div className="d-flex align-items-center ms-3 flex-shrink-0">
            <div className="d-flex align-items-center">
              <button className="btn btn-outline-secondary" onClick={handleMinus}>−</button>
              <span className="mx-3 fw-bold">{item.quantity}</span>
              <button className="btn btn-outline-primary" onClick={handlePlus}>+</button>
            </div>

            <div className="fw-bold ms-3">{item.price * item.quantity} ₽</div>

            <button
              className="btn btn-outline-danger ms-3"
              onClick={() => setShowModal(true)}
              title="Удалить товар"
            >
              🗑️
            </button>
          </div>

        </div>
      </div>

      {/* Модальное окно */}
      {showModal && (
        <div className="modal show d-block" tabIndex="-1" role="dialog">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Подтвердите удаление</h5>
                <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
              </div>
              <div className="modal-body">
                <p>Вы уверены, что хотите удалить <strong>{item.title}</strong> из корзины?</p>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Отмена</button>
                <button type="button" className="btn btn-danger" onClick={handleRemove}>Удалить</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
