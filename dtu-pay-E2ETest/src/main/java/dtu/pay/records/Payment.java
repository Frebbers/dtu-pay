<<<<<<<< HEAD:dtu-pay-E2ETest/src/main/java/dtu/pay/records/Payment.java
package dtu.pay.records;

import dtu.pay.models.User;
========
package dtu.pay.models;
>>>>>>>> payment-service:dtu-pay-server/src/main/java/dtu/pay/models/Payment.java

public record Payment(int amount, User customer, User merchant) {}

