package de.sciss.connect

import de.sciss.synth.UGenSpec
import de.sciss.serial.{DataOutput, DataInput, ImmutableSerializer}

// No need to write the actual spec, just look it up in the dictionary
object UGenSpecSerializer extends ImmutableSerializer[UGenSpec] {
  def write(v: UGenSpec, out: DataOutput) {
    out.writeUTF(v.name)
  }

  def read(in: DataInput): UGenSpec = UGenSpec.standardUGens(in.readUTF())
}