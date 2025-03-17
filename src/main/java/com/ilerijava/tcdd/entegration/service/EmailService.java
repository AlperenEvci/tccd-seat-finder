package com.ilerijava.tcdd.entegration.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

import com.ilerijava.tcdd.entegration.DTO.TrainAvailableSeatsDTO;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendAvailableSeatsEmail(List<TrainAvailableSeatsDTO> availableTrains, String fromStation,
            String toStation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("YOUR_EMAIL");
            helper.setTo("TO_YOUR_EMAIL");
            helper.setSubject("TCDD Boş Koltuk Bildirimi");

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<h2>TCDD Boş Koltuk Bildirimi</h2>");
            htmlContent.append("<p>Aşağıdaki seferlerde boş koltuk bulundu:</p>");
            htmlContent.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            htmlContent.append("<tr style='background-color: #f2f2f2;'>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Sefer Adı</th>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Kalkış Saati</th>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Kalkış İstasyonu</th>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Varış İstasyonu</th>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Koltuk Tipi</th>" +
                    "<th style='padding: 12px 15px; text-align: left;'>Boş Koltuk Sayısı</th></tr>");

            for (TrainAvailableSeatsDTO train : availableTrains) {
                if (train.getSeatInfo().getTotalSeats() > 0) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td style='padding: 12px 15px;'>").append(train.getTrainName()).append("</td>");
                    htmlContent.append("<td style='padding: 12px 15px;'>").append(train.getDepartureTime())
                            .append("</td>");
                    htmlContent.append("<td style='padding: 12px 15px;'>").append(fromStation).append("</td>");
                    htmlContent.append("<td style='padding: 12px 15px;'>").append(toStation).append("</td>");
                    htmlContent.append("<td style='padding: 12px 15px;'>EKONOMİ</td>");
                    htmlContent.append("<td style='padding: 12px 15px;'>").append(train.getSeatInfo().getTotalSeats())
                            .append("</td>");
                    htmlContent.append("</tr>");
                }
            }

            htmlContent.append("</table>");
            htmlContent.append(
                    "<p>Bilet satın almak için <a href='https://ebilet.tcddtasimacilik.gov.tr/'>TCDD Bilet Satış</a> sayfasını ziyaret edebilirsiniz.</p>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Mail gönderimi sırasında hata oluştu: " + e.getMessage());
        }
    }
}