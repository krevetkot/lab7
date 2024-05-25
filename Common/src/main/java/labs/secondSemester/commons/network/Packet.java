package labs.secondSemester.commons.network;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Packet implements Serializable {
    private Header header;
    private byte[] pieceOfBuffer;

    public Packet(Header header, byte[] pieceOfBuffer){
        this.header = header;
        this.pieceOfBuffer = pieceOfBuffer;
    }
}
