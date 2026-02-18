import { createContext, useContext, useState, useEffect } from "react";
import { getCart, addToCart, updateQuantity } from "../api/cart.api";
import { useAuth } from "./AuthContext";

const CartContext = createContext();
export const useCart = () => useContext(CartContext);

export const CartProvider = ({ children }) => {
  const { user } = useAuth();

  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(false);

  const refreshCart = async () => {
    if (!user) {
      setCartItems([]);
      return;
    }

    setLoading(true);
    try {
      const res = await getCart({ page: 0, size: 1000 });
      setCartItems(res.data.content || []);
    } catch {
      setCartItems([]);
    } finally {
      setLoading(false);
    }
  };

  // 🔥 Грузим корзину ТОЛЬКО когда есть user
  useEffect(() => {
    if (!user) {
      setCartItems([]);
      return;
    }

    refreshCart();
  }, [user]);

  // 🔎 Проверка наличия товара
  const findByProductId = (productId) =>
    cartItems.find(item => item.productId === productId);

  // ➕ Добавить или увеличить
  const addOrIncrease = async (productId) => {
    if (!user) return; // защита

    const existing = findByProductId(productId);

    if (existing) {
      await updateQuantity(existing.id, existing.quantity + 1);
    } else {
      await addToCart(productId);
    }

    await refreshCart();
  };

  // 🧮 Количество товаров
  const cartCount = cartItems.reduce(
    (sum, item) => sum + item.quantity,
    0
  );

  return (
    <CartContext.Provider
      value={{
        cartItems,
        cartCount,
        loading,
        refreshCart,
        addOrIncrease,
        findByProductId
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
