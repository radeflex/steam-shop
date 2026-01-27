import { useEffect, useState } from "react";
import {
  getCart,
  updateQuantity,
  removeFromCart
} from "../api/cart.api";
import { purchaseProducts } from "../api/product.api";
import CartCard from "../components/CartCard";

export default function CartPage() {
  const [items, setItems] = useState([]);

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

  const total = items.reduce(
    (sum, i) => sum + i.price * i.quantity,
    0
  );

  const purchase = async () => {
    const orders = items.map(i => ({
      productId: i.productId,
      quantity: i.quantity
    }));

    await purchaseProducts(orders);
    alert("Purchase completed successfully");
    loadCart();
  };

  return (
    <div className="container mt-4">
      <div className="row">

        {/* PRODUCTS */}
        <div className="col-md-8">
          <h3>Shopping Cart</h3>

          {items.length === 0 && (
            <p className="text-muted">Your cart is empty</p>
          )}

          {items.map(item => (
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
                onClick={purchase}
              >
                Checkout
              </button>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
