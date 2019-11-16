package io.github.incplusplus.peerprocessing.linear;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
@JsonSubTypes({@JsonSubTypes.Type(RealMatrix.class)})
public abstract class Matrix<T> {
  /** @return the number of rows in this matrix */
  @JsonIgnore
  public abstract int getNumRows();

  /** @return the number of columns in this matrix */
  @JsonIgnore
  public abstract int getNumCols();

  public boolean isSquare() {
    return this.getNumRows() == this.getNumCols();
  }
}
