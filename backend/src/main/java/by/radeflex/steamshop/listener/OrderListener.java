package by.radeflex.steamshop.listener;

import by.radeflex.steamshop.entity.PaymentSource;
import by.radeflex.steamshop.event.payment.ProcessOrderEvent;
import by.radeflex.steamshop.event.payment.CreateOrderEvent;
import by.radeflex.steamshop.repository.UserProductRepository;
import by.radeflex.steamshop.service.MailService;
import by.radeflex.steamshop.service.NotificationService;
import by.radeflex.steamshop.service.UserProductHistoryService;
import by.radeflex.steamshop.service.payment.AccountService;
import by.radeflex.steamshop.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import me.dynomake.yookassa.exception.BadRequestException;
import me.dynomake.yookassa.exception.UnspecifiedShopInformation;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Transactional
public class OrderListener {
    private final NotificationService notificationService;
    private final AccountService accountService;
    private final MailService mailService;
    private final UserProductRepository userProductRepository;
    private final UserProductHistoryService userProductHistoryService;
    private final PaymentService paymentService;

    @EventListener
    public void handleCreateOrder(CreateOrderEvent e) {
        notificationService.sendPayment(e.getPayment());
    }

    @EventListener
    public void handleProcessOrder(ProcessOrderEvent e) throws UnspecifiedShopInformation, BadRequestException, IOException {
        var p = e.getPayment();
        try {
            var accounts = accountService.sellAccounts(p);
            mailService.sendAccounts(p, accounts);
        } catch (Exception ex) {
            if (p.getConfirmationUrl() != null)
                paymentService.createRefund(p);
        }
        notificationService.sendPayment(p);
        userProductHistoryService.saveHistory(p);
        if (p.getSource() == PaymentSource.CART)
            userProductRepository.deleteAllByUser(p.getUser());
    }
}
