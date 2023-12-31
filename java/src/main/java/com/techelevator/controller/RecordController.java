package com.techelevator.controller;

import com.techelevator.dao.*;
import com.techelevator.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/")
@CrossOrigin
public class RecordController {

    private RecordDao recordDao;

    private UserDao userDao;

    private CollectionDao collectionDao;

    private ArtistDao artistDao;

    private GenreDao genreDao;

    public RecordController(RecordDao recordDao, UserDao userDao, CollectionDao collectionDao, ArtistDao artistDao, GenreDao genreDao) {
        this.recordDao = recordDao;
        this.userDao = userDao;
        this.collectionDao = collectionDao;
        this.artistDao = artistDao;
        this.genreDao = genreDao;
    }

    @GetMapping("/records")
    public List<Record2point0> getRecords() {
        List<Record2point0> records = recordDao.getRecords();

        for (Record2point0 record : records) {
            List<Collection> collections = collectionDao.getCollectionsByRecordId(record.getRecordId());
            List<Artist> artists = artistDao.getArtistsByRecordId(record.getRecordId());
            List<Genre> genres = genreDao.getGenresByRecordId(record.getRecordId());

            record.setCollections(collections);
            record.setArtists(artists);
            record.setGenres(genres);
        }

        return records;
    }

//    @GetMapping("records")
//    public List<Record> getCurrentUserRecords(Principal principal) {
//        User loggedInUser = userDao.getUserByEmailAddress(principal.getName());
//
//        List<Record> records = recordDao.getRecordsByUserId(loggedInUser.getId());
//        if (records == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Records Not Found");
//        }
//
//        return records;
//    }

    @GetMapping("records/{recordId}")
    public Record getRecordByRecordId(@PathVariable int recordId) {

        Record record = recordDao.getRecordById(recordId);
        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record Details Not Found");
        }
        return record;
    }


    @GetMapping("users/{userId}/records")
    public List<Record> getRecordsByUserId(@PathVariable int userId, Principal principal) {
        User loggedInUser = userDao.getUserByEmailAddress(principal.getName());

        List<Record> records = recordDao.getRecordsByUserId(userId);
        if (records == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Records Not Found");
        }

        String roles = "";
        List<Authority> authorities = new ArrayList<>(loggedInUser.getAuthorities());
        for (Authority authority : authorities) {
            roles += authority.getName() + " ";
        }

        if (loggedInUser.getId() != userId) {
            if (!roles.contains("ROLE_ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That collection is private and you are not the owner");
            }
        }

        return records;
    }

    @GetMapping("/users/{userId}/records/amount")
    public int getNumOfRecordsByUserId(@PathVariable int userId) {
        return recordDao.getNumOfRecordsByUserId(userId);
    }

    @GetMapping("collections/{collectionId}/records")
    public List<Record> getRecordsByCollectionId(@PathVariable int collectionId, Principal principal) {

        Collection collection = collectionDao.getCollectionById(collectionId);

        if (!collection.isPublic()) {
            User loggedInUser = userDao.getUserByEmailAddress(principal.getName());

            String roles = "";
            List<Authority> authorities = new ArrayList<>(loggedInUser.getAuthorities());
            for (Authority authority : authorities) {
                roles += authority.getName() + " ";
            }

            if (loggedInUser.getId() != collection.getUserId()) {
                if (!roles.contains("ROLE_ADMIN")) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "That collection is private and you are not the owner");
                }
            }
        }

        List<Record> records = recordDao.getRecordsByCollectionId(collectionId);
        if (records == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Records Not Found");
        }

        return records;
    }


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "record", method = RequestMethod.POST)
    public Record createRecord( @RequestBody Record newRecord, Principal principal) {
        User loggedInUser = userDao.getUserByEmailAddress(principal.getName());
        if (loggedInUser.getId() != newRecord.getUserId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record could not be added");
        }
        Record dbRecord = recordDao.createRecord(newRecord);
        if (dbRecord == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create record");
        }

        return dbRecord;
    }

    @PutMapping("records/{recordId}")
    public Record editRecordNotes(@PathVariable int recordId, @RequestBody Record newRecord) {
//        Record record = recordDao.getRecordById(recordId);
        Record updatedRecord = recordDao.updateNotes(newRecord);

        if (updatedRecord == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No notes to add");
        }
        return updatedRecord;
    }



}
