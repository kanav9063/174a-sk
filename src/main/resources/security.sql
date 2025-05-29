-- ====================================================
-- PIN Security Functions and Triggers
-- ====================================================

-- Function to hash PINs using SHA-256
create or replace function hash_pin (
   p_pin varchar2
) return varchar2
   deterministic
is
begin
    -- Use DBMS_CRYPTO to hash the PIN with SHA-256
    -- Returns lowercase hex string (64 characters)
   return lower(rawtohex(dbms_crypto.hash(
      utl_i18n.string_to_raw(
         p_pin,
         'AL32UTF8'
      ),
      dbms_crypto.hash_sh256
   )));
end hash_pin;
/

-- Trigger to automatically hash PINs on INSERT or UPDATE
create or replace trigger hash_pin_trigger before
   insert or update of pin on student
   for each row
   when ( new.pin is not null )
begin
    -- Only hash if the PIN looks unhashed (less than 64 chars)
    -- This prevents double-hashing
   if length(:new.pin) < 64 then
      :new.pin := hash_pin(:new.pin);
   end if;
end;
/
