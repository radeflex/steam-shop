import { useEffect, useState, useRef } from "react";
import { getProducts } from "../api/product.api";
import ProductCard from "../components/ProductCard";

const initialFilter = {
    title: "",
    priceMin: "",
    priceMax: ""
};

export default function ProductsPage() {
    const [products, setProducts] = useState([]);
    const [pageNumber, setPageNumber] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const [filter, setFilter] = useState(initialFilter);

    const [productOptions, setProductOptions] = useState([]);
    const [loading, setLoading] = useState(false);

    const loaderRef = useRef(null);

    const pageNumberRef = useRef(pageNumber);
    const totalPagesRef = useRef(totalPages);
    const loadingRef = useRef(loading);

    useEffect(() => {
        pageNumberRef.current = pageNumber;
        totalPagesRef.current = totalPages;
        loadingRef.current = loading;
    }, [pageNumber, totalPages, loading]);

    const fetchProducts = (page = 0, append = false, activeFilter = filter) => {
        if (loadingRef.current || (append && page >= totalPagesRef.current)) return;

        setLoading(true);

        const filterPayload = {
            title: activeFilter.title || undefined,
            priceMin: activeFilter.priceMin ? Number(activeFilter.priceMin) : undefined,
            priceMax: activeFilter.priceMax ? Number(activeFilter.priceMax) : undefined,
        };

        const pageable = { page, size: 8 };

        getProducts(filterPayload, pageable)
            .then((r) => {
                const newProducts = r.data.content || [];

                setProducts((prev) =>
                    append ? [...prev, ...newProducts] : newProducts
                );

                setPageNumber(page);
                setTotalPages(r.data.totalPages || 1);

                setProductOptions(r.data.content.map((p) => p.title));
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        fetchProducts(0, false, filter);
    }, []);
    const handleSubmit = (e) => {
        e.preventDefault();
        fetchProducts(0, false, filter);
    };

    // reset all filters
    const handleReset = () => {
        setFilter(initialFilter);
        fetchProducts(0, false, initialFilter);
    };

    // infinite scroll
    useEffect(() => {
        const observer = new IntersectionObserver(
            (entries) => {
                const target = entries[0];

                if (
                    target.isIntersecting &&
                    pageNumberRef.current + 1 < totalPagesRef.current &&
                    !loadingRef.current
                ) {
                    fetchProducts(pageNumberRef.current + 1, true);
                }
            },
            {
                root: null,
                rootMargin: "150px",
                threshold: 0,
            }
        );

        const el = loaderRef.current;
        if (el) observer.observe(el);

        return () => {
            if (el) observer.unobserve(el);
            observer.disconnect();
        };
    }, []);

    const handleChange = (e) => {
        setFilter((prev) => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    };

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Products</h2>

            {/* Filter */}
            <form className="row g-3 mb-4" onSubmit={handleSubmit}>
                <div className="col-md-4">
                    <label className="form-label">Product Title</label>
                    <input
                        type="text"
                        className="form-control"
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
                        name="priceMin"
                        value={filter.priceMin}
                        onChange={handleChange}
                    />
                </div>

                <div className="col-md-2">
                    <label className="form-label">Max Price</label>
                    <input
                        type="number"
                        className="form-control"
                        name="priceMax"
                        value={filter.priceMax}
                        onChange={handleChange}
                    />
                </div>

                <div className="col-md-2 d-flex align-items-end gap-2">
                    <button type="submit" className="btn btn-primary w-100">
                        Filter
                    </button>
                </div>

                <div className="col-md-2 d-flex align-items-end">
                    <button
                        type="button"
                        className="btn btn-outline-secondary w-100"
                        onClick={handleReset}
                    >
                        Reset
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

            {/* trigger */}
            <div ref={loaderRef} style={{ height: "40px" }} />
        </div>
    );
}