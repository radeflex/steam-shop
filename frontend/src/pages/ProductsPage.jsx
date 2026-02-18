// ProductsPage.jsx
import { useEffect, useState, useRef } from "react";
import { getProducts } from "../api/product.api";
import ProductCard from "../components/ProductCard";

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [pageNumber, setPageNumber] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filter, setFilter] = useState({ title: "", priceMin: "", priceMax: "" });
  const [productOptions, setProductOptions] = useState([]);
  const [loading, setLoading] = useState(false);

  const pageNumberRef = useRef(pageNumber);
  const totalPagesRef = useRef(totalPages);
  const loadingRef = useRef(loading);

  // Обновляем ref при изменении состояния
  useEffect(() => {
    pageNumberRef.current = pageNumber;
    totalPagesRef.current = totalPages;
    loadingRef.current = loading;
  }, [pageNumber, totalPages, loading]);

  const fetchProducts = (page = 0, append = false) => {
    // Не грузим, если все страницы загружены или уже идёт загрузка
    if (loadingRef.current || (append && page >= totalPagesRef.current)) return;

    setLoading(true);

    const filterPayload = {
      title: filter.title || undefined,
      priceMin: filter.priceMin ? Number(filter.priceMin) : undefined,
      priceMax: filter.priceMax ? Number(filter.priceMax) : undefined,
    };
    const pageable = { page, size: 8 };

    getProducts(filterPayload, pageable)
      .then((r) => {
        const newProducts = r.data.content || [];
        if (append && newProducts.length === 0) return;

        setProducts((prev) => (append ? [...prev, ...newProducts] : newProducts));
        setPageNumber(page);
        setTotalPages(r.data.totalPages || 1);

        // Обновляем автокомплит
        const titles = r.data.content.map((p) => p.title);
        setProductOptions(titles);
      })
      .finally(() => setLoading(false));
  };

  // Инициализация и scroll listener один раз
  useEffect(() => {
    fetchProducts(0, false); // первая загрузка

    const handleScroll = () => {
      if (
        window.innerHeight + window.scrollY >=
          document.documentElement.scrollHeight - 100 &&
        pageNumberRef.current + 1 < totalPagesRef.current &&
        !loadingRef.current
      ) {
        fetchProducts(pageNumberRef.current + 1, true);
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [filter]); // если фильтр меняется, подгружаем заново

  const handleChange = (e) => {
    setFilter({ ...filter, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchProducts(0, false); // при фильтре сбрасываем страницу
  };

  return (
    <div className="container mt-4">
      <h2 className="mb-4">Products</h2>

      {/* Filter Form */}
      <form className="row g-3 mb-4" onSubmit={handleSubmit}>
        <div className="col-md-4">
          <label className="form-label">Product Title</label>
          <input
            type="text"
            className="form-control"
            placeholder="Search or select product"
            name="title"
            value={filter.title}
            onChange={handleChange}
            list="product-titles"
          />
          <datalist id="product-titles">
            {productOptions.map((title, idx) => (
              <option key={idx} value={title} />
            ))}
          </datalist>
        </div>

        <div className="col-md-2">
          <label className="form-label">Min Price</label>
          <input
            type="number"
            className="form-control"
            placeholder="0"
            name="priceMin"
            value={filter.priceMin}
            onChange={handleChange}
            min={0}
          />
        </div>

        <div className="col-md-2">
          <label className="form-label">Max Price</label>
          <input
            type="number"
            className="form-control"
            placeholder="1000"
            name="priceMax"
            value={filter.priceMax}
            onChange={handleChange}
            min={0}
          />
        </div>

        <div className="col-md-2 d-flex align-items-end">
          <button type="submit" className="btn btn-primary w-100">
            Filter
          </button>
        </div>
      </form>

      {!loading && products.length === 0 && <p>No products found.</p>}

      <div className="row">
        {products.map((p) => (
          <div className="col-md-3 mb-4" key={p.id}>
            <ProductCard product={p} />
          </div>
        ))}
      </div>

      {loading && <p className="text-center">Loading more products...</p>}
    </div>
  );
}
