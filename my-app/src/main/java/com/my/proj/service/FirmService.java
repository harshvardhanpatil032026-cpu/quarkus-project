package com.my.proj.service;

import java.time.Instant;
import java.util.List;

import com.my.proj.dto.FirmRequest;
import com.my.proj.entity.Firm;
import com.my.proj.entity.FirmStatus;
import com.my.proj.exception.FirmCodeAlreadyExistsException;
import com.my.proj.exception.FirmNotFoundException;
import com.my.proj.repository.FirmRepository;

import org.hibernate.exception.ConstraintViolationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FirmService {

    private final FirmRepository firmRepository;

    public FirmService(FirmRepository firmRepository) {
        this.firmRepository = firmRepository;
    }

    public List<Firm> list() {
        return firmRepository.listAll();
    }

    public Firm get(Long id) {
        Firm firm = firmRepository.findById(id);
        if (firm == null) {
            throw new FirmNotFoundException(id);
        }
        return firm;
    }

    @Transactional
    public Firm create(FirmRequest request) {
        String code = request.code().trim();
        if (firmRepository.codeTaken(code, null)) {
            throw new FirmCodeAlreadyExistsException(code);
        }
        Firm firm = new Firm();
        apply(firm, request, code);
        firm.setStatus(FirmStatus.ACTIVE);
        Instant now = Instant.now();
        firm.setCreatedAt(now);
        firm.setUpdatedAt(now);
        firmRepository.persist(firm);
        flushOrConflict(code);
        return firm;
    }

    @Transactional
    public Firm update(Long id, FirmRequest request) {
        Firm firm = get(id);
        String code = request.code().trim();
        if (firmRepository.codeTaken(code, id)) {
            throw new FirmCodeAlreadyExistsException(code);
        }
        apply(firm, request, code);
        if (request.status() != null) {
            firm.setStatus(FirmStatus.valueOf(request.status()));
        }
        firm.setUpdatedAt(Instant.now());
        flushOrConflict(code);
        return firm;
    }

    /** Soft delete: marks the firm INACTIVE; the row is kept. Deleting an inactive firm is a no-op. */
    @Transactional
    public void deactivate(Long id) {
        Firm firm = get(id);
        if (firm.getStatus() != FirmStatus.INACTIVE) {
            firm.setStatus(FirmStatus.INACTIVE);
            firm.setUpdatedAt(Instant.now());
        }
    }

    /**
     * The code check above is look-then-write, so two concurrent requests can both pass it; the
     * database's unique constraint then rejects the second, which is reported as the same 409.
     */
    private void flushOrConflict(String code) {
        try {
            firmRepository.flush();
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new FirmCodeAlreadyExistsException(code);
            }
            throw e;
        }
    }

    private static void apply(Firm firm, FirmRequest r, String code) {
        firm.setName(r.name().trim());
        firm.setCode(code);
        firm.setContactEmail(r.contactEmail());
        firm.setPhone(r.phone());
        firm.setAddress(r.address());
    }
}
