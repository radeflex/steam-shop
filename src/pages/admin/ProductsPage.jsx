import { useEffect, useState } from 'react';
import { getProducts, deleteProduct } from '../../api/product.api';
import { Link } from 'react-router-dom';

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await getProducts({ page: 0, size: 20 });
      setProducts(res.data.content || []);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      await deleteProduct(id);
      fetchProducts();
    }
  };

  useEffect(() => { fetchProducts(); }, []);

  if (loading) return <div className="text-center p-3">Loading...</div>;

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3>Products</h3>
        <Link to="/admin/products/create" className="btn btn-success">Create Product</Link>
      </div>

      <table className="table table-striped table-hover">
        <thead className="table-dark">
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Description</th>
            <th>Price</th>
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
              <td>
                <Link to={`/admin/products/edit/${p.id}`} className="btn btn-primary btn-sm me-2">Edit</Link>
                <button onClick={() => handleDelete(p.id)} className="btn btn-danger btn-sm">Delete</button>
              </td>
            </tr>
          )) : "No products found."}
        </tbody>
      </table>
    </div>
  );
}
