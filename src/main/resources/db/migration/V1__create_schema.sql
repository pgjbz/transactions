create table if not exists accounts(
    id bigserial primary key,
    document_number varchar(14) not null unique
);

create table if not exists operations_types(
    id bigint primary key,
    description varchar(50)
);

create table if not exists transactions(
    id bigserial primary key,
    account_id bigint not null,
    operation_type bigint not null,
    amount numeric(20,2) not null,
    event_date timestamp DEFAULT current_timestamp
);

ALTER TABLE transactions ADD CONSTRAINT fk_transactions_accounts FOREIGN KEY (account_id) REFERENCES accounts (id);
ALTER TABLE transactions ADD CONSTRAINT fk_transactions_operations_types FOREIGN KEY (operation_type) REFERENCES operations_types (id);