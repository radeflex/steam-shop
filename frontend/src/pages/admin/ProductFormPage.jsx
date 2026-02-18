import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createProduct, getProduct, updateProduct } from '../../api/product.api';

export default function AdminProductFormPage() {
  const { id } = useParams();
  const isEdit = !!id;

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [image, setImage] = useState(null);
  const [originalData, setOriginalData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    if (isEdit) {
      getProduct(id)
        .then(res => {
          const p = res.data;
          setTitle(p.title);
          setDescription(p.description);
          setPrice(p.price);
          setOriginalData({
            title: p.title,
            description: p.description,
            price: p.price,
          });
        })
        .catch(err => {
          console.error(err);
        });
    }
  }, [id, isEdit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors({});

    try {
      let data = {};

      if (!isEdit) {
        // Для создания передаем все данные
        data = { title, description, price: price ? parseInt(price) : null };
      } else {
        // Для редактирования добавляем только измененные поля
        if (title !== originalData.title) data.title = title;
        if (description !== originalData.description) data.description = description;
        if (parseInt(price) !== originalData.price) data.price = price ? parseInt(price) : null;

        // Если ничего не изменилось и изображения нет — редиректим
        if (Object.keys(data).length === 0 && !image) {
          navigate('/admin/products');
          return;
        }
      }

      if (isEdit) {
        await updateProduct(id, data, image);
      } else {
        await createProduct(data, image);
      }

      navigate('/admin/products');
    } catch (err) {
      console.error(err);
      if (err.response?.data?.errors) setErrors(err.response.data.errors);
    } finally {
      setLoading(false);
    }
  };

  const handleDiscard = () => {
    navigate('/admin/products');
  };

  return (
    <div className="container mt-4">
      <h3>{isEdit ? 'Edit Product' : 'Create Product'}</h3>
      <form onSubmit={handleSubmit} className="mt-3">
        <div className="mb-3">
          <label className="form-label">Title</label>
          <input
            type="text"
            className={`form-control ${errors.title ? 'is-invalid' : ''}`}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
            minLength={3}
            maxLength={32}
          />
          {errors.title && <div className="invalid-feedback">{errors.title}</div>}
        </div>

        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea
            className={`form-control ${errors.description ? 'is-invalid' : ''}`}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
            rows={4}
          ></textarea>
          {errors.description && <div className="invalid-feedback">{errors.description}</div>}
        </div>

        <div className="mb-3">
          <label className="form-label">Price</label>
          <input
            type="number"
            className={`form-control ${errors.price ? 'is-invalid' : ''}`}
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            required
            min={1}
          />
          {errors.price && <div className="invalid-feedback">{errors.price}</div>}
        </div>

        <div className="mb-3">
          <label className="form-label">Image</label>
          <input
            type="file"
            className="form-control"
            onChange={(e) => setImage(e.target.files[0])}
            accept="image/*"
          />
        </div>

        <div className="d-flex gap-2">
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? (isEdit ? 'Saving...' : 'Creating...') : (isEdit ? 'Save Product' : 'Create Product')}
          </button>
          <button type="button" className="btn btn-secondary" onClick={handleDiscard} disabled={loading}>
            Discard
          </button>
        </div>
      </form>
    </div>
  );
}
