-- Deleting a user removes everything they own in the database itself,
-- instead of Hibernate loading and deleting each row one by one.
-- The foreign keys also get readable names (fk_<table>_<column>) in place of
-- the hash names Hibernate generated for V1.

ALTER TABLE ONLY public.financial_goals
    DROP CONSTRAINT fk27wrsw0lbfwxa5fqsg810f58f,
    ADD CONSTRAINT fk_financial_goals_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.categories
    DROP CONSTRAINT fkghuylkwuedgl2qahxjt8g41kb,
    ADD CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.budgets
    DROP CONSTRAINT fkln0tm5tgf3f9q3sp9sa5m8m7b,
    ADD CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.transactions
    DROP CONSTRAINT fkqwv7rmvc8va8rep7piikrojds,
    ADD CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

ALTER TABLE public.budgets
    RENAME CONSTRAINT fkn7qib00712y8dwelmqfwis6ka TO fk_budgets_category;

ALTER TABLE public.transactions
    RENAME CONSTRAINT fksqqi7sneo04kast0o138h19mv TO fk_transactions_category;
