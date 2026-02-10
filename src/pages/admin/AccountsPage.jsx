import React, { useEffect, useMemo, useState, useRef } from "react";
import api from "../../api/axios";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

const PAGE_SIZE = 20;

export default function AdminAccountsPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [accounts, setAccounts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [csvLoading, setCsvLoading] = useState(false);

  // inline create row
  const [creatingRow, setCreatingRow] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [createForm, setCreateForm] = useState({
    username: "",
    password: "",
    email: "",
    emailPassword: "",
    productId: "",
  });

  const fileInputRef = useRef(null);

  async function loadAccounts() {
    setLoading(true);
    setError("");

    try {
      const params = new URLSearchParams();
      params.set("pageable.page", String(page));
      params.set("pageable.size", String(PAGE_SIZE + 1));
      params.append("pageable.sort", "id,desc");

      const res = await api.get(`/accounts?${params.toString()}`);
      const data = res.data;

      let list = [];
      if (Array.isArray(data)) list = data;
      else if (Array.isArray(data?.content)) list = data.content;
      else if (Array.isArray(data?.data)) list = data.data;
      else {
        const firstArray = Object.values(data ?? {}).find((v) =>
          Array.isArray(v)
        );
        if (Array.isArray(firstArray)) list = firstArray;
      }

      if (list.length > PAGE_SIZE) {
        setHasNext(true);
        list = list.slice(0, PAGE_SIZE);
      } else {
        setHasNext(false);
      }

      setAccounts(list);
    } catch (e) {
      setError(
        e?.response?.data?.message || e?.message || "Ошибка загрузки аккаунтов"
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAccounts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const columns = useMemo(() => {
    const first = accounts?.[0];
    if (!first || typeof first !== "object") return [];
    return Object.keys(first);
  }, [accounts]);

  function renderCell(value) {
    if (value === null || value === undefined) return "-";
    if (typeof value === "object") {
      return (
        <pre className="bg-light p-2 rounded mb-0 small">
          {JSON.stringify(value, null, 2)}
        </pre>
      );
    }
    return String(value);
  }

  const showPagination = page > 0 || hasNext;

  function openCreateRow() {
    setCreateError("");
    setFieldErrors({});
    setCreatingRow(true);
  }

  function cancelCreateRow() {
    if (createLoading) return;
    setCreateError("");
    setFieldErrors({});
    setCreatingRow(false);
    setCreateForm({
      username: "",
      password: "",
      email: "",
      emailPassword: "",
      productId: "",
    });
  }

  async function createAccount() {
    setCreateLoading(true);
    setCreateError("");
    setFieldErrors({});

    try {
      const payload = {
        username: createForm.username.trim(),
        password: createForm.password,
        email: createForm.email.trim(),
        emailPassword: createForm.emailPassword,
        productId: Number(createForm.productId),
      };

      if (
        !payload.username ||
        !payload.password ||
        !payload.email ||
        !payload.emailPassword ||
        Number.isNaN(payload.productId)
      ) {
        setCreateError("Заполни все поля (productId должен быть числом)");
        setCreateLoading(false);
        return;
      }

      await api.post("/accounts", payload);
      cancelCreateRow();
      await loadAccounts();
    } catch (e) {
      const data = e?.response?.data;
      if (data?.errors && typeof data.errors === "object") {
        setFieldErrors(data.errors);
      } else {
        setCreateError(data?.message || e?.message || "Ошибка создания аккаунта");
      }
    } finally {
      setCreateLoading(false);
    }
  }

  function renderInput(field, type = "text", placeholder) {
    return (
      <>
        <input
          type={type}
          className={`form-control form-control-sm ${
            fieldErrors[field] ? "is-invalid" : ""
          }`}
          placeholder={placeholder}
          value={createForm[field]}
          onChange={(e) =>
            setCreateForm((f) => ({ ...f, [field]: e.target.value }))
          }
        />
        {fieldErrors[field] && (
          <div className="invalid-feedback">{fieldErrors[field]}</div>
        )}
      </>
    );
  }

  // === CSV Import ===
  const handleCsvChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    setCsvLoading(true); // включаем loading

    try {
      const res = await api.post("/accounts", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const data = res.data;

      if (data.errorRows && data.errorRows.length > 0) {
        toast.warn(
          `Импорт завершен с ошибками. Всего: ${data.total}, вставлено: ${data.inserted}, ошибки в строках: ${data.errorRows.join(
            ", "
          )}`
        );
      } else {
        toast.success(
          `Импорт завершен успешно. Всего: ${data.total}, вставлено: ${data.inserted}`
        );
      }

      await loadAccounts();
    } catch (err) {
      toast.warn(err?.response?.data?.message || err?.message || "Ошибка импорта CSV");
    } finally {
      setCsvLoading(false); // выключаем loading
      e.target.value = null; // сброс input
    }
  };


  return (
    <div className="container py-4">
      <ToastContainer position="top-right" autoClose={5000} />
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="d-flex justify-content-end gap-2 mb-3">
        <input
          type="file"
          ref={fileInputRef}
          accept=".csv"
          style={{ display: "none" }}
          onChange={handleCsvChange}
        />
        <button
          className="btn btn-info"
          onClick={() => fileInputRef.current?.click()}
          disabled={csvLoading}
        >
          {csvLoading ? "Import..." : "Import"}
        </button>
        <button
          className="btn btn-primary"
          disabled={creatingRow || loading}
          onClick={openCreateRow}
          title="Добавить аккаунт"
        >
          +raw
        </button>
      </div>


      <div className="card">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0 align-middle">
              <thead className="table-light">
                <tr>
                  {columns.map((c) => (
                    <th key={c}>{c}</th>
                  ))}
                  <th style={{ width: 140 }} />
                </tr>
              </thead>

              <tbody>
                {creatingRow && (
                  <>
                    {createError && (
                      <tr>
                        <td colSpan={Math.max(columns.length + 1, 1)}>
                          <div className="alert alert-danger m-2 mb-0">
                            {createError}
                          </div>
                        </td>
                      </tr>
                    )}

                    <tr>
                      <td>{renderInput("username", "text", "username")}</td>
                      <td>{renderInput("password", "password", "password")}</td>
                      <td>{renderInput("email", "text", "email")}</td>
                      <td>{renderInput("emailPassword", "password", "emailPassword")}</td>
                      <td>{renderInput("productId", "text", "productId")}</td>

                      <td className="text-end">
                        <div className="d-flex justify-content-end gap-2">
                          <button
                            className="btn btn-sm btn-success"
                            disabled={createLoading}
                            onClick={createAccount}
                          >
                            {createLoading ? "..." : "✓"}
                          </button>
                          <button
                            className="btn btn-sm btn-outline-danger"
                            disabled={createLoading}
                            onClick={cancelCreateRow}
                          >
                            ✕
                          </button>
                        </div>
                      </td>
                    </tr>
                  </>
                )}

                {loading && (
                  <tr>
                    <td colSpan={Math.max(columns.length + 1, 1)}>
                      <div className="text-center py-4">Загрузка...</div>
                    </td>
                  </tr>
                )}

                {!loading && accounts.length === 0 && !creatingRow && (
                  <tr>
                    <td colSpan={Math.max(columns.length + 1, 1)}>
                      <div className="text-center py-4 text-muted">
                        Аккаунтов нет
                      </div>
                    </td>
                  </tr>
                )}

                {!loading &&
                  accounts.map((a, idx) => (
                    <tr key={a.id ?? idx}>
                      {columns.map((c) => (
                        <td key={c}>{renderCell(a?.[c])}</td>
                      ))}
                      <td />
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>

        {showPagination && (
          <div className="card-footer d-flex justify-content-between align-items-center">
            <button
              className="btn btn-outline-secondary"
              disabled={loading || page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              ← Назад
            </button>

            <div className="text-muted">Страница: {page + 1}</div>

            <button
              className="btn btn-outline-secondary"
              disabled={loading || !hasNext}
              onClick={() => setPage((p) => p + 1)}
            >
              Вперёд →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
