package by.radeflex.steamshop.repository.impl;

import by.radeflex.steamshop.entity.AccountStatus;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.repository.ProductRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static by.radeflex.steamshop.entity.QAccount.account;
import static by.radeflex.steamshop.entity.QProduct.product;

@Component
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    private final EntityManager em;
    @Override
    public Page<Product> findAllAvailable(ProductFilter filter, Pageable pageable) {
        var queryFactory = new JPAQueryFactory(em);

        BooleanBuilder builder = new BooleanBuilder();
        if (filter.title() != null) builder.and(product.title.containsIgnoreCase(filter.title()));
        if (filter.priceMin() != null) builder.and(product.price.goe(filter.priceMin()));
        if (filter.priceMax() != null) builder.and(product.price.loe(filter.priceMax()));

        var query = queryFactory.selectFrom(product)
                .join(account)
                .on(product.id.eq(account.product.id))
                .where(builder.and(account.status.eq(AccountStatus.AVAILABLE)))
                .groupBy(product.id)
                .having(account.count().gt(0))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(query);
    }
}
