package com.formas.cms.leads;

import com.formas.cms.orders.Order;
import com.formas.cms.orders.OrderItem;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LeadNotificationService {
  private static final Logger logger = LoggerFactory.getLogger(LeadNotificationService.class);

  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final String recipient;
  private final String sender;

  public LeadNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
      @Value("${formas.notifications.to:contacto@formasinteriores.com}") String recipient,
      @Value("${formas.notifications.from:contacto@formasinteriores.com}") String sender) {
    this.mailSenderProvider = mailSenderProvider;
    this.recipient = recipient;
    this.sender = sender;
  }

  public void notifyContactSubmission(ContactSubmission submission) {
    send(
        "Nueva solicitud de cotizacion - Formas Interiores",
        String.join("\n",
            "Llego una nueva solicitud desde el formulario de contacto.",
            "",
            "Nombre: " + value(submission.name),
            "Telefono: " + value(submission.phone),
            "Correo: " + value(submission.email),
            "Interes: " + value(submission.interest),
            "",
            "Mensaje:",
            value(submission.message),
            "",
            "Fecha: " + submission.createdAt),
        submission.email);
  }

  public void notifyNewsletterSubscription(NewsletterSubscription subscription) {
    send(
        "Nueva suscripcion al blog - Formas Interiores",
        String.join("\n",
            "Una persona solicito suscribirse al blog/newsletter.",
            "",
            "Correo: " + value(subscription.email),
            "Fecha: " + subscription.createdAt),
        subscription.email);
  }

  public void notifyCustomerOrder(Order order) {
    send(
        "Nueva solicitud de compra o cotizacion - Formas Interiores",
        String.join("\n",
            "Se registro una solicitud desde el carrito.",
            "",
            "Referencia: " + value(order.reference),
            "Tipo: " + value(order.paymentProvider),
            "Metodo: " + value(order.paymentMethod),
            "Total: " + money(order.amountCents),
            "Subtotal: " + money(order.subtotalCents),
            "IVA: " + money(order.taxCents),
            "",
            "Cliente:",
            "Nombre: " + value(order.customer.name),
            "Telefono: " + value(order.customer.phone),
            "Correo: " + value(order.customer.email),
            "Ciudad: " + value(order.customer.city),
            "Direccion: " + value(order.customer.address),
            "Notas: " + value(order.customer.notes),
            "",
            "Productos:",
            order.items.stream().map(this::formatItem).collect(Collectors.joining("\n")),
            "",
            "Fecha: " + order.createdAt),
        order.customer.email);
  }

  private void send(String subject, String body, String replyTo) {
    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      logger.warn("No hay servidor SMTP configurado. Notificacion no enviada a {}", recipient);
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(recipient);
      message.setFrom(sender);
      message.setReplyTo(value(replyTo).equals("No informado") ? sender : replyTo.trim());
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
    } catch (MailException error) {
      logger.warn("No se pudo enviar notificacion a {}", recipient, error);
    }
  }

  private String formatItem(OrderItem item) {
    return "- " + value(item.name) + " | Categoria: " + value(item.category)
        + " | Cantidad: " + item.quantity + " | Unitario: " + money(item.unitAmountCents)
        + " | Total: " + money(item.totalAmountCents);
  }

  private String money(Long cents) {
    if (cents == null || cents <= 0) return "Por cotizar";
    return "$" + String.format("%,d", cents / 100).replace(',', '.') + " COP";
  }

  private String value(String text) {
    return text == null || text.isBlank() ? "No informado" : text.trim();
  }
}