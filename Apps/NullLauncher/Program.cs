using System.Diagnostics;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace NullLauncher;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new LauncherForm());
    }
}

internal sealed class LocalAccount
{
    public string Email { get; set; } = "";
    public string Login { get; set; } = "";
    public string Salt { get; set; } = "";
    public string PasswordHash { get; set; } = "";
}

internal sealed class LauncherForm : Form
{
    private readonly string minecraft = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");
    private readonly string dataDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "NULL");
    private readonly string accountsFile;
    private readonly string activeFile;
    private readonly TextBox email = new();
    private readonly TextBox login = new();
    private readonly TextBox password = new();
    private readonly TextBox repeatPassword = new();
    private readonly TextBox launcherPath = new();
    private readonly Label status = new();
    private readonly TabControl tabs = new();

    public LauncherForm()
    {
        accountsFile = Path.Combine(dataDir, "accounts.json");
        activeFile = Path.Combine(minecraft, "NULL", "active-account.txt");
        Directory.CreateDirectory(dataDir);
        Text = "NULL Launcher";
        Width = 650; Height = 590;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        BackColor = Color.FromArgb(14, 16, 22);
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10);

        var title = new Label { Text = "NULL", Font = new Font("Segoe UI", 32, FontStyle.Bold), ForeColor = Color.FromArgb(190, 130, 255), AutoSize = true, Left = 28, Top = 12 };
        var subtitle = new Label { Text = "LOCAL ACCOUNT  /  MINECRAFT 1.21.4", AutoSize = true, Left = 34, Top = 74, ForeColor = Color.Silver };
        tabs.SetBounds(30, 112, 575, 285);
        tabs.DrawMode = TabDrawMode.Normal;
        tabs.TabPages.Add(BuildRegisterTab());
        tabs.TabPages.Add(BuildLoginTab());

        var pathLabel = new Label { Text = "Путь к Legacy Launcher:", AutoSize = true, Left = 34, Top = 410, ForeColor = Color.Gainsboro };
        launcherPath.SetBounds(34, 437, 435, 30);
        launcherPath.Text = LoadLauncherPath();
        var browse = MakeButton("ОБЗОР", 480, 436, 125, 32);
        browse.Click += (_, _) => ChooseLegacyLauncher();
        var install = MakeButton("ОБНОВИТЬ NULL", 34, 480, 275, 42);
        install.Click += (_, _) => InstallMod();
        var play = MakeButton("PLAY  /  LEGACY LAUNCHER", 324, 480, 281, 42);
        play.BackColor = Color.FromArgb(85, 48, 135);
        play.Click += (_, _) => LaunchLegacy();
        status.SetBounds(34, 530, 570, 32);
        status.ForeColor = Color.Gainsboro;
        status.Text = "Аккаунты хранятся локально на этом ПК. Вход Minecraft выполняется отдельно.";
        Controls.AddRange([title, subtitle, tabs, pathLabel, launcherPath, browse, install, play, status]);
        InstallMod();
    }

    private TabPage BuildRegisterTab()
    {
        var page = MakeTab("Регистрация");
        AddField(page, "Почта", email, 18);
        AddField(page, "Логин (будет виден в NULL)", login, 70);
        AddField(page, "Пароль", password, 122, true);
        AddField(page, "Повтори пароль", repeatPassword, 174, true);
        var button = MakeButton("СОЗДАТЬ АККАУНТ", 340, 216, 200, 34);
        button.Click += (_, _) => Register();
        page.Controls.Add(button);
        return page;
    }

    private TabPage BuildLoginTab()
    {
        var page = MakeTab("Вход");
        var loginEmail = new TextBox();
        var loginPassword = new TextBox { UseSystemPasswordChar = true };
        AddField(page, "Почта", loginEmail, 45);
        AddField(page, "Пароль", loginPassword, 105, true);
        var button = MakeButton("ВОЙТИ В NULL", 340, 166, 200, 38);
        button.Click += (_, _) => Login(loginEmail.Text, loginPassword.Text);
        page.Controls.Add(button);
        return page;
    }

    private TabPage MakeTab(string title) => new(title) { BackColor = Color.FromArgb(20, 23, 32), ForeColor = Color.White };

    private void AddField(Control parent, string caption, TextBox box, int top, bool secret = false)
    {
        var label = new Label { Text = caption, Left = 18, Top = top + 3, Width = 190, ForeColor = Color.Gainsboro };
        box.SetBounds(215, top, 325, 28);
        box.UseSystemPasswordChar = secret;
        parent.Controls.Add(label);
        parent.Controls.Add(box);
    }

    private Button MakeButton(string text, int x, int y, int w, int h)
    {
        var b = new Button { Text = text, Left = x, Top = y, Width = w, Height = h, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 46, 56), ForeColor = Color.White, Cursor = Cursors.Hand };
        b.FlatAppearance.BorderSize = 0;
        return b;
    }

    private List<LocalAccount> ReadAccounts()
    {
        try { return File.Exists(accountsFile) ? JsonSerializer.Deserialize<List<LocalAccount>>(File.ReadAllText(accountsFile)) ?? [] : []; }
        catch { return []; }
    }

    private void SaveAccounts(List<LocalAccount> accounts)
    {
        Directory.CreateDirectory(dataDir);
        File.WriteAllText(accountsFile, JsonSerializer.Serialize(accounts, new JsonSerializerOptions { WriteIndented = true }));
    }

    private static string HashPassword(string password, byte[] salt) =>
        Convert.ToBase64String(Rfc2898DeriveBytes.Pbkdf2(password, salt, 210000, HashAlgorithmName.SHA256, 32));

    private void Register()
    {
        var mail = email.Text.Trim();
        var name = login.Text.Trim();
        if (!mail.Contains('@') || !mail.Contains('.') || mail.Length > 254) { ShowStatus("Введи корректную почту."); return; }
        if (name.Length < 3 || name.Length > 16 || name.Any(ch => !char.IsLetterOrDigit(ch) && ch != '_')) { ShowStatus("Логин: 3–16 символов, буквы, цифры или _."); return; }
        if (password.Text.Length < 8) { ShowStatus("Пароль должен содержать минимум 8 символов."); return; }
        if (password.Text != repeatPassword.Text) { ShowStatus("Пароли не совпадают."); return; }
        var accounts = ReadAccounts();
        if (accounts.Any(a => a.Email.Equals(mail, StringComparison.OrdinalIgnoreCase))) { ShowStatus("Аккаунт с такой почтой уже есть на этом ПК."); return; }
        if (accounts.Any(a => a.Login.Equals(name, StringComparison.OrdinalIgnoreCase))) { ShowStatus("Этот логин уже занят на этом ПК."); return; }
        var salt = RandomNumberGenerator.GetBytes(16);
        accounts.Add(new LocalAccount { Email = mail, Login = name, Salt = Convert.ToBase64String(salt), PasswordHash = HashPassword(password.Text, salt) });
        SaveAccounts(accounts);
        Activate(name);
        ShowStatus($"Аккаунт {name} создан. Ты вошёл в NULL.");
    }

    private void Login(string mail, string pass)
    {
        var account = ReadAccounts().FirstOrDefault(a => a.Email.Equals(mail.Trim(), StringComparison.OrdinalIgnoreCase));
        if (account is null) { ShowStatus("Аккаунт с такой почтой не найден на этом ПК."); return; }
        try
        {
            var salt = Convert.FromBase64String(account.Salt);
            var actual = Convert.FromBase64String(HashPassword(pass, salt));
            var expected = Convert.FromBase64String(account.PasswordHash);
            if (!CryptographicOperations.FixedTimeEquals(actual, expected)) { ShowStatus("Неверный пароль."); return; }
            Activate(account.Login);
            ShowStatus($"Вход выполнен. Привет, {account.Login}!");
        }
        catch { ShowStatus("Не удалось проверить данные аккаунта."); }
    }

    private void Activate(string name)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(activeFile)!);
        File.WriteAllText(activeFile, name);
    }

    private void ShowStatus(string message) => status.Text = message;

    private string launcherPathFile => Path.Combine(dataDir, "legacy-launcher-path.txt");
    private string LoadLauncherPath() { try { return File.Exists(launcherPathFile) ? File.ReadAllText(launcherPathFile).Trim() : ""; } catch { return ""; } }

    private void ChooseLegacyLauncher()
    {
        using var dialog = new OpenFileDialog { Title = "Выбери LegacyLauncher.exe", Filter = "Программы (*.exe)|*.exe" };
        if (dialog.ShowDialog() != DialogResult.OK) return;
        launcherPath.Text = dialog.FileName;
        File.WriteAllText(launcherPathFile, dialog.FileName);
        ShowStatus("Путь к Legacy Launcher сохранён.");
    }

    private void InstallMod()
    {
        try
        {
            var mods = Path.Combine(minecraft, "mods");
            Directory.CreateDirectory(mods);
            using var source = Assembly.GetExecutingAssembly().GetManifestResourceStream("NULL.jar")
                ?? throw new FileNotFoundException("В сборку лаунчера не встроен NULL.jar.");
            var targetPath = Path.Combine(mods, "NULL-Visual-1.2.0.jar");
            using (var target = File.Create(targetPath)) source.CopyTo(target);
            foreach (var oldJar in Directory.GetFiles(mods, "NULL-Visual-*.jar"))
                if (!string.Equals(oldJar, targetPath, StringComparison.OrdinalIgnoreCase)) File.Delete(oldJar);
            ShowStatus("NULL 1.2.0 готов. Мод автоматически установлен в .minecraft/mods.");
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error); }
    }

    private void LaunchLegacy()
    {
        try
        {
            var exe = launcherPath.Text.Trim().Trim('"');
            if (!File.Exists(exe)) { ShowStatus("Укажи LegacyLauncher.exe через кнопку ОБЗОР."); return; }
            File.WriteAllText(launcherPathFile, exe);
            Process.Start(new ProcessStartInfo(exe) { UseShellExecute = true, WorkingDirectory = Path.GetDirectoryName(exe)! });
            ShowStatus("Legacy Launcher открыт. Выбери Fabric 1.21.4 и выполни вход там.");
        }
        catch (Exception ex) { MessageBox.Show(ex.Message, "NULL Launcher", MessageBoxButtons.OK, MessageBoxIcon.Error); }
    }
}
