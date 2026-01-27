import { getImageUrl } from "../api/file.api";
import { useCart } from "../context/CartContext";

export default function ProductCard({ product }) {
  const { addOrIncrease, findByProductId } = useCart();

  const inCart = findByProductId(product.id);

  return (
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

        <div className="mt-auto">
          <button
            className={`btn w-100 ${inCart ? "btn-success" : "btn-primary"}`}
            onClick={() => addOrIncrease(product.id)}
          >
            {inCart ? "Add one more" : "Add to cart"}
          </button>
        </div>
      </div>
    </div>
  );
}
