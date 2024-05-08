package ch.uzh.ifi.hase.soprafs24.agora;

public interface PackableEx extends Packable {
  void unmarshal(ByteBuf in);
}
