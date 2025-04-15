package com.bozzat.esepkersoft.Services;

import com.bozzat.esepkersoft.Models.Distributor;

import java.util.List;
import java.util.Map;

public class DistributorService {
    private dbManager db = dbManager.getInstance();

    public boolean addDistributor(Distributor distributor) {
        if (distributor == null || distributor.getName() == null || distributor.getName().trim().isEmpty()) {
            return false;
        }

        String query = "INSERT INTO distributors " +
                "(name, contact_person, phone, email, address, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))";

        return db.executeSet(query,
                distributor.getName().trim(),
                distributor.getContactPerson(),
                distributor.getPhone(),
                distributor.getEmail(),
                distributor.getAddress(),
                distributor.isActive()
        );
    }

    public boolean updateDistributor(Distributor distributor) {
        if (distributor == null || distributor.getId() <= 0) {
            return false;
        }

        String query = "UPDATE distributors SET " +
                "name = ?, contact_person = ?, phone = ?, " +
                "email = ?, address = ?, active = ? " +
                "WHERE id = ?";

        return db.executeSet(query,
                distributor.getName(),
                distributor.getContactPerson(),
                distributor.getPhone(),
                distributor.getEmail(),
                distributor.getAddress(),
                distributor.isActive(),
                distributor.getId()
        );
    }

    public boolean deleteDistributor(int distributorId) {
        if (distributorId <= 0) {
            return false;
        }

        return db.executeSet("DELETE FROM distributors WHERE id = ?", distributorId);
    }

    public Distributor getDistributorById(int distributorId) {
        if (distributorId <= 0) {
            return null;
        }

        String query = "SELECT * FROM distributors WHERE id = ?";
        List<Map<String, Object>> results = db.executeGet(query, distributorId);

        if (results.isEmpty()) {
            return null;
        }

        Map<String, Object> data = results.get(0);
        return new Distributor(
                ((Number) data.get("id")).intValue(),
                (String) data.get("name"),
                (String) data.get("contact_person"),
                (String) data.get("phone"),
                (String) data.get("email"),
                (String) data.get("address"),
                (Boolean) data.get("active"),
                (String) data.get("created_at")
        );
    }

    public List<Distributor> getAllDistributors() {
        String query = "SELECT * FROM distributors ORDER BY name";
        List<Map<String, Object>> results = db.executeGet(query);

        return results.stream()
                .map(row -> new Distributor(
                        ((Number) row.get("id")).intValue(),
                        (String) row.get("name"),
                        (String) row.get("contact_person"),
                        (String) row.get("phone"),
                        (String) row.get("email"),
                        (String) row.get("address"),
                        (Boolean) row.get("active"),
                        (String) row.get("created_at")
                ))
                .toList();
    }
}
