package com.example.filesharing.repository;

import com.example.filesharing.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    /**
     * Finds all files with a delete time before the specified current time.
     *
     * @param currentTime The current time to compare against.
     * @return A list of expired files.
     */
    List<FileEntity> findByDeleteTimeBefore(LocalDateTime currentTime);

    /**
     * Finds a file by its file name.
     *
     * @param fileName The name of the file to search for.
     * @return An Optional containing the file if found, otherwise empty.
     */
    Optional<FileEntity> findByFileName(String fileName);
}