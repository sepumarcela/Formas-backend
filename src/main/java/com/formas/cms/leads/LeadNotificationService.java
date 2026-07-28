package com.formas.cms.leads;

import com.formas.cms.orders.Order;
import com.formas.cms.orders.OrderItem;
import java.util.Arrays;
import java.util.List;
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
  private static final String DEFAULT_RECIPIENTS =
      "contacto@formasinteriores.com,hello-11@51669439.hubspot-inbox.com";

  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final List<String> recipients;
  private final String sender;

  public LeadNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
      @Value("${formas.notifications.to:" + DEFAULT_RECIPIENTS + "}") String recipient,
      @Value("${formas.notifications.from:contacto@formasinteriores.com}") String sender) {
    this.mailSenderProvider = mailSenderProvider;
    this.recipients = parseRecipients(recipient);
    this.sender = sender;
  }

  public void notifyContactSubmission(ContactSubmission submission) {
    String customerEmail = value(submission.email);

    send(
        "Lead cotizacion - " + customerEmail,
        String.join("\n",
            "Correo del cliente: " + customerEmail,
            "Nombre del cliente: " + value(submission.name),
            "Telefono del cliente: " + value(submission.phone),
            "",
            "Llego una nueva solicitud desde el formulario de contacto.",
            "",
            "Nombre: " + value(submission.name),
            "Telefono: " + value(submission.phone),
            "Correo: " + customerEmail,
            "Interes: " + value(submission.interest),
            "",
            "Mensaje:",
            value(submission.message),
            "",
            "Fecha: " + submission.createdAt),
        submission.email);

    sendCustomerConfirmation(submission);
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
        orderEmailBody(order, "Se registro una solicitud desde el carrito."),
        order.customer.email);
  }

  public void notifyApprovedOrder(Order order) {
    send(
        "Pedido pagado para despachar - Formas Interiores",
        orderEmailBody(order, "Wompi confirmo el pago. Este pedido ya puede pasar a revision y despacho."),
        order.customer.email);
  }

  private String orderEmailBody(Order order, String intro) {
    return String.join("\n",
        intro,
        "",
        "Referencia: " + value(order.reference),
        "Tipo: " + value(order.paymentProvider),
        "Estado: " + order.status,
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
        "Fecha: " + order.createdAt);
  }

  private void send(String subject, String body, String replyTo) {
    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      logger.warn("No hay servidor SMTP configurado. Notificacion no enviada a {}", recipients);
      return;
    }

    if (recipients.isEmpty()) {
      logger.warn("No hay destinatarios configurados para notificaciones.");
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(recipients.toArray(String[]::new));
      message.setFrom(sender);
      message.setReplyTo(value(replyTo).equals("No informado") ? sender : replyTo.trim());
      message.setSubject(subject);
      message.setText(body);
      mailSender.send(message);
    } catch (MailException error) {
      logger.warn("No se pudo enviar notificacion a {}", recipients, error);
    }
  }

  private void sendCustomerConfirmation(ContactSubmission submission) {
    String customerEmail = value(submission.email);
    if (customerEmail.equals("No informado")) {
      return;
    }

    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      logger.warn("No hay servidor SMTP configurado. Confirmacion no enviada a {}", customerEmail);
      return;
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(customerEmail);
      message.setFrom(sender);
      message.setReplyTo(sender);
      message.setSubject("Recibimos tu solicitud - Formas Interiores");
      message.setText(String.join("\n",
          "Hola " + value(submission.name) + ",",
          "",
          "Gracias por contactar a Formas Interiores.",
          "",
          "Hemos recibido correctamente la informaci\u00f3n de tu proyecto. "
              + "Uno de nuestros asesores revisar\u00e1 tu solicitud y se pondr\u00e1 en contacto contigo "
              + "en el menor tiempo posible.",
          "",
          "Espacios de inter\u00e9s: " + value(submission.interest),
          "",
          "Si deseas agregar informaci\u00f3n, puedes responder directamente a este correo.",
          "",
          "Un saludo,",
          "Equipo Formas Interiores"));
      mailSender.send(message);
    } catch (MailException error) {
      logger.warn("No se pudo enviar confirmacion a {}", customerEmail, error);
    }
  }

  private List<String> parseRecipients(String recipient) {
    return Arrays.stream(value(recipient).split("[,;]"))
        .map(String::trim)
        .filter(email -> !email.isBlank() && !email.equals("No informado"))
        .toList();
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