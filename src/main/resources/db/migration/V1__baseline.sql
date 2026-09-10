CREATE TABLE public.budgets (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    monthly_limit numeric(12,2) NOT NULL,
    category_id uuid,
    user_id uuid NOT NULL
);

CREATE TABLE public.categories (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(500),
    category_name character varying(255) NOT NULL,
    user_id uuid NOT NULL
);

CREATE TABLE public.financial_goals (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    current_amount numeric(38,2),
    deadline timestamp(6) without time zone NOT NULL,
    description character varying(255),
    name character varying(255) NOT NULL,
    target_amount numeric(38,2) NOT NULL,
    user_id uuid NOT NULL
);

CREATE TABLE public.transactions (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    amount numeric(38,2) NOT NULL,
    transaction_date timestamp(6) without time zone NOT NULL,
    transaction_type character varying(255) NOT NULL,
    category_id uuid,
    user_id uuid NOT NULL,
    deleted_at timestamp(6) without time zone,
    CONSTRAINT transactions_transaction_type_check CHECK (((transaction_type)::text = ANY ((ARRAY['INCOME'::character varying, 'EXPENSE'::character varying])::text[])))
);

CREATE TABLE public.users (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    surname character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone
);

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.financial_goals
    ADD CONSTRAINT financial_goals_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT uki2tekvq944to55i83r6uwfelw UNIQUE (user_id, category_id);

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT ukjgn1bmvdb04yskabufl986qvj UNIQUE (user_id, category_name);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.financial_goals
    ADD CONSTRAINT fk27wrsw0lbfwxa5fqsg810f58f FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT fkghuylkwuedgl2qahxjt8g41kb FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT fkln0tm5tgf3f9q3sp9sa5m8m7b FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT fkn7qib00712y8dwelmqfwis6ka FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT fkqwv7rmvc8va8rep7piikrojds FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT fksqqi7sneo04kast0o138h19mv FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE SET NULL;
