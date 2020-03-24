Bridge:

Ares Profiles
User-end process:
Player joins for the first time and account is created
Player runs command /account create
    - A session with a 30 minute expire time is created and they are presented a link which will be www.example.com/user/<ares-uuid>/create
        - Redirects to a page where they must put in an password and confirm the password

All account commands:
/account create
/account resetpassword
/account settings
    Settings:
    Toggle private messages
    Toggle broadcasts
    Toggle auto-accept network invites
    Toggle snitch notifications
    Require login

All account-based web post requests:
www.example.com/user/<ares-uuid>/create
www.example.com/user/<ares-uuid>/reset

Ares Profile Data:
Ares UUID
Bukkit UUID
Bukkit Username
IP Address
Account Settings
    - Toggle private messages (bool)
    - Toggle broadcasts (bool)
    - Toggle auto-accept network invites (bool)
    - Toggle snitch notifications (bool)
    - Toggle require login

Bridge Database Layout
AccountCreateSession
    Ares UUID
    Bukkit UUID
    Expire time (long millis)

AccountResetSession
    Ares UUID
    Bukkit UUID
    Expire time (long millis)

