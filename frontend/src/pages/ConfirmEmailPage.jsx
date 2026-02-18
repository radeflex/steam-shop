import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function ConfirmEmailPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get("token");

  const [status, setStatus] = useState("loading");
  // loading | success | expired

  useEffect(() => {
    if (!token) {
      setStatus("error");
      return;
    }

    api
      .get(`/confirm-email`, {
        params: { token },
      })
      .then(() => {
        setStatus("success");
      })
      .catch(() => {
          setStatus("expired");
      });
  }, [token]);

  const goToProducts = () => navigate("/products");

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card shadow-sm">
            <div className="card-body text-center">
              {status === "loading" && (
                <>
                  <h4>⏳ Подтверждаем email…</h4>
                  <p className="text-muted">Пожалуйста, подождите</p>
                </>
              )}

              {status === "success" && (
                <>
                  <h4 className="text-success">✅ Email подтверждён</h4>
                  <p>Спасибо! Теперь вы можете пользоваться аккаунтом.</p>
                  <button className="btn btn-primary mt-3" onClick={goToProducts}>
                    Перейти к товарам
                  </button>
                </>
              )}

              {status === "expired" && (
                <>
                  <h4 className="text-danger">❌ Ссылка недействительна</h4>
                  <p>
                    Возможно, ссылка устарела или уже была использована.
                  </p>
                  <button className="btn btn-outline-primary mt-3" onClick={goToProducts}>
                    На главную
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
