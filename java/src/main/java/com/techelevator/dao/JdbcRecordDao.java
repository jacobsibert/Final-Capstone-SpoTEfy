package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Record;
import com.techelevator.model.Record2point0;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcRecordDao implements RecordDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Record2point0> getRecords() {
        List<Record2point0> records = new ArrayList<>();
        String sql = "SELECT record_id, user_id, album_name, album_cover, release_date, media_type, record_notes " +
                "FROM records";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                records.add(mapRowToRecord2point0(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return records;
    }

    public List<Record> getRecordsByCollectionId(int id) {
        List<Record> records = new ArrayList<>();
        String sql = "SELECT r.record_id, user_id, album_name, album_cover, release_date, media_type, record_notes " +
                     "FROM records r " +
                     "JOIN collections_records cr ON r.record_id = cr.record_id " +
                     "WHERE collection_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            while (results.next()) {
                records.add(mapRowToRecord(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return records;
    }

    public List<Record> getRecordsByUserId(int userId) {
        List<Record> records = new ArrayList<>();
        String sql = "SELECT record_id, user_id, album_name, album_cover, release_date, media_type, record_notes FROM records WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                records.add(mapRowToRecord(results));
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return records;
    }

    public Record getRecordById(int recordId) {
        Record record = null;
        String recordIdSQL = "SELECT record_id, user_id, album_name, album_cover, release_date, media_type, record_notes\n" +
                "FROM records\n" +
                "WHERE record_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(recordIdSQL, recordId);
            if (results.next()) {
                record = mapRowToRecord(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return record;
    }

    public int getNumOfRecordsByUserId(int userId) {
        String sql = "SELECT COUNT(record_id) AS num_of_records FROM records WHERE user_id = ? GROUP BY user_id";
        int numOfRecords = -1;
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
            if (result.next()) {
                numOfRecords = result.getInt("num_of_records");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return numOfRecords;
    }

    public Record createRecord(Record record) {
        Record newRecord;

        String createRecordSQL = "INSERT INTO records(\n" +
                "\tuser_id, album_name, album_cover, release_date, media_type)\n" +
                "\tVALUES (?, ?, ?, ?, ?) RETURNING record_id;";
        try {
            int newRecordId = jdbcTemplate.queryForObject(createRecordSQL, int.class, record.getUserId(),
                    record.getAlbumName(), record.getAlbumCover(), record.getReleaseDate(), record.getMediaType());
            newRecord = getRecordById(newRecordId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newRecord;
    }

    public Record updateNotes(Record record) {

        String updateNotesSql = "UPDATE records\n" +
                "SET record_notes = ?\n" +
                "WHERE record_id = ?;";

        int rowsUpdate = jdbcTemplate.update(updateNotesSql, record.getRecordNotes(), record.getRecordId());

        if (rowsUpdate > 0 ) {
            return record;
        } else {
            return null;
        }
    }

    private Record mapRowToRecord(SqlRowSet rs) {
        Record record = new Record();
        record.setRecordId(rs.getInt("record_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setAlbumName(rs.getString("album_name"));
        record.setAlbumCover(rs.getString("album_cover"));
        if (rs.getDate("release_date") != null) {
            record.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        record.setMediaType(rs.getString("media_type"));
        record.setRecordNotes(rs.getString("record_notes"));
        return record;
    }

    private Record2point0 mapRowToRecord2point0(SqlRowSet rs) {
        Record2point0 record = new Record2point0();
        record.setRecordId(rs.getInt("record_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setAlbumName(rs.getString("album_name"));
        record.setAlbumCover(rs.getString("album_cover"));
        if (rs.getDate("release_date") != null) {
            record.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        record.setMediaType(rs.getString("media_type"));
        record.setRecordNotes(rs.getString("record_notes"));
        return record;
    }
}
