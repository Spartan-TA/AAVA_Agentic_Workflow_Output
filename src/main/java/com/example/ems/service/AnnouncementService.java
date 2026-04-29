package com.example.ems.service;

import com.example.ems.entity.Announcement;
import com.example.ems.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public Optional<Announcement> getAnnouncementById(Long id) {
        return announcementRepository.findById(id);
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(Long id, Announcement updatedAnnouncement) {
        return announcementRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedAnnouncement.getTitle());
                    existing.setMessage(updatedAnnouncement.getMessage());
                    existing.setCreatedAt(updatedAnnouncement.getCreatedAt());
                    existing.setExpiresAt(updatedAnnouncement.getExpiresAt());
                    existing.setLocale(updatedAnnouncement.getLocale());
                    existing.setStatus(updatedAnnouncement.getStatus());
                    return announcementRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }
}
