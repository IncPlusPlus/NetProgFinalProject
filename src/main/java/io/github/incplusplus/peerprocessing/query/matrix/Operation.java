package io.github.incplusplus.peerprocessing.query.matrix;

public enum Operation {
  TRANSPOSE(1, false),
  ADD(1, false),
  SUBTRACT(1, false),
  MULTIPLY(2, true);

  private final int involvedMatrices;
  private final boolean supportsSplitting;

  Operation(int involvedMatrices, boolean supportsSplitting) {
    this.involvedMatrices = involvedMatrices;
    this.supportsSplitting = supportsSplitting;
  }

  /** @return the number of matrices this operation involves */
  public int getInvolvedMatrices() {
    return involvedMatrices;
  }
}
