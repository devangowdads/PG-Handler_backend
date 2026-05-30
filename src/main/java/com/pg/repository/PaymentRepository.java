package com.pg.repository;
import com.pg.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByGuestId(String guestId);
    List<Payment> findByMode(Payment.PaymentMode mode);
}
