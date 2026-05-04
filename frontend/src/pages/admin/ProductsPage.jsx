import { useEffect, useState } from 'react';
import { getAdminProducts, deleteProduct } from '../../api/product.api';
import { Link } from 'react-router-dom';

const initialFilter = {
  title: "",
  priceMin: "",
  priceMax: ""
};

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState(initialFilter);

  const fetchProducts = async (activeFilter = filter) => {
    setLoading(true);
    try {
      const filterPayload = {
        title: activeFilter.title || undefined,
        priceMin: activeFilter.priceMin ? Number(activeFilter.priceMin) : undefined,
        priceMax: activeFilter.priceMax ? Number(activeFilter.priceMax) : undefined,
      };

      const res = await getAdminProducts(filterPayload, { page: 0, size: 20 });
      setProducts(res.data.content || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      await deleteProduct(id);
      fetchProducts();
    }
  };

  const handleChange = (e) => {
    setFilter((prev) => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchProducts(filter);
  };

  const handleReset = () => {
    setFilter(initialFilter);
    fetchProducts(initialFilter);
  };

  if (loading) return <div className="text-center p-3">Loading...</div>;

  return (
      <div className="container mt-4">

        <div className="d-flex justify-content-between align-items-center mb-3">
          <h3>Products</h3>
          <Link to="/admin/products/create" className="btn btn-success">
            Create Product
          </Link>
        </div>

        {/* 🔽 FILTER (как во 2 компоненте) */}
        <form className="row g-3 mb-4" onSubmit={handleSubmit}>
          <div className="col-md-4">
            <label className="form-label">Product Title</label>
            <input
                type="text"
                className="form-control"
                name="title"
                value={filter.title}
                onChange={handleChange}
            />
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

          <div className="col-md-2 d-flex align-items-end">
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

        <table className="table table-striped table-hover">
          <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Description</th>
            <th>Price</th>
            <th>Left</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          {products.length ? products.map(p => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.title}</td>
                <td>{p.description}</td>
                <td>{p.price} ₽</td>
                <td style={{ color: p.left ? "black" : "red" }}>
                  {p.left}
                </td>
                <td>
                  <Link
                      to={`/admin/products/edit/${p.id}`}
                      className="btn btn-primary btn-sm me-2"
                  >
                    Edit
                  </Link>
                  <button
                      onClick={() => handleDelete(p.id)}
                      className="btn btn-danger btn-sm"
                  >
                    Delete
                  </button>
                </td>
              </tr>
          )) : (
              <tr>
                <td colSpan="6" className="text-center">
                  No products found.
                </td>
              </tr>
          )}
          </tbody>
        </table>
      </div>
  );
}