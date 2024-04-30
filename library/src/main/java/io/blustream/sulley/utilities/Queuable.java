package io.blustream.sulley.utilities;

import androidx.annotation.NonNull;

/**
 * Created by Ruzhitskii Sviatoslav on 11/18/19.
 */
public interface Queuable {
    void setTransactionFinishListener(@NonNull TransactionsFinishListener transactionsFinishListener);
}
