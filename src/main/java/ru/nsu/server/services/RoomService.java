package ru.nsu.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.server.model.Room;
import ru.nsu.server.repository.RoomRepository;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }


    public List<Room> getAllRooms() {
        return roomRepository.getAll();
    }

    public boolean ifExistByRoomName(String roomName) {
        return roomRepository.existsByName(roomName);
    }

    @Transactional
    public void saveNewRoom(String name, String type, int capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCapacity(capacity);
        room.setType(type);
        roomRepository.save(room);
    }
}
