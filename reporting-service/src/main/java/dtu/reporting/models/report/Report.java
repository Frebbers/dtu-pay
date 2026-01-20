package dtu.reporting.models.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Report<T extends ReportEntry> {
    private final List<T> payments = new ArrayList<>();

    public void addEntry(T entry) {
        payments.add(entry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report<?> that = (Report<?>) o;
        return Objects.equals(payments, that.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payments);
    }
}
