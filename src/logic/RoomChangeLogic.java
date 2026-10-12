package logic;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Logic ของหน้าขอย้ายห้อง (ไม่มี Swing) */
public class RoomChangeLogic {

    public static final String TRANSFER_FILE = "room_transfers.csv";
    public static final String TRANSFER_HEADER =
            "transferId,userId,contractId,oldRoomId,newRoomId,reason,requestDate,status";

    public enum Status { INVALID_INPUT, PENDING_EXISTS, SUCCESS }

    public static class Result {
        public final Status status;
        public final String transferId; // มีค่าเมื่อ SUCCESS

        Result(Status status, String transferId) {
            this.status = status;
            this.transferId = transferId;
        }
    }

    private final LogicLogin.User user;
    private final DataStore.Room current;
    private final DataStore.Contract contract;
    private final List<DataStore.Room> available;

    public RoomChangeLogic(LogicLogin.User user) {
        this.user = user;
        this.current = (user == null) ? null : DataStore.room(user.roomId);
        this.contract = DataStore.contractFor(user);
        this.available = (current == null)
                ? new ArrayList<>()
                : DataStore.availableRooms(current.id);
    }

    public boolean hasRoom() {
        return current != null;
    }

    public DataStore.Room currentRoom() {
        return current;
    }

    public List<DataStore.Room> availableRooms() {
        return available;
    }

    public Result submit(DataStore.Room target, String reason) throws IOException {
        if (target == null || reason.isEmpty()) {
            return new Result(Status.INVALID_INPUT, null);
        }
        if (DataStore.hasPendingTransfer(user.userId)) {
            return new Result(Status.PENDING_EXISTS, null);
        }

        String transferId = DataStore.nextId(TRANSFER_FILE, "transferId", "T", 3);
        DataStore.appendRow(TRANSFER_FILE, TRANSFER_HEADER,
                transferId,
                DataStore.blank(user.userId) ? "-" : user.userId,
                contract == null ? "-" : contract.id,
                current.id,
                target.id,
                reason,
                LocalDate.now().toString(),
                "PENDING");
        return new Result(Status.SUCCESS, transferId);
    }
}
