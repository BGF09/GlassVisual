using System.Diagnostics;
using System.Reflection;
using System.Security.Cryptography;
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
    private readonly Color background = Color.FromArgb(10, 12, 18);
    private readonly Color surface = Color.FromArgb(19, 22, 31);
    private readonly Color surface2 = Color.FromArgb(27, 31, 43);
    private readonly Color accent = Color.FromArgb(153, 105, 255);
    private bool fullScreen;
    private Rectangle previousBounds;
    private FormBorderStyle previousBorderStyle;
    private FormWindowState previousWindowState;

    public LauncherForm()
    {
        accountsFile = Path.Combine(dataDir, "accounts.json");
        activeFile = Path.Combine(minecraft, "NULL", "active-account.txt");
        Directory.CreateDirectory(dataDir);

        Text = "NULL Launcher";
        StartPosition = FormStartPosition.CenterScreen;
        Size = new Size(1120, 760);
        MinimumSize = new Size(900, 650);
        FormBorderStyle = FormBorderStyle.None;
        BackColor = background;
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10);
        KeyPreview = true;
        KeyDown += (_, e) => { if (e.KeyCode == Keys.F11) { ToggleFullScreen(); e.Handled = true; } };

        var root = new TableLayoutPanel { Dock = DockStyle.Fill, BackColor = background, ColumnCount = 1, RowCount = 3, Padding = new Padding(1) };
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 54));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        root.RowStyles.Add(new RowStyle(SizeType.Absolute, 78));
        Controls.Add(root);

        var topBar = new Panel { Dock = DockStyle.Fill, BackColor = surface, Padding = new Padding(22, 0, 12, 0) };
        var brand = new Label { Text = "N U L L", Dock = DockStyle.Left, Width = 170, TextAlign = ContentAlignment.MiddleLeft, Font = new Font("Segoe UI", 15, FontStyle.Bold), ForeColor = Color.White };
        var topHint = new Label { Text = "VISUAL CLIENT  /  MINECRAFT 1.21.4", Dock = DockStyle.Fill, TextAlign = ContentAlignment.MiddleLeft, ForeColor = Color.FromArgb(145, 151, 168), Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };
        var windowButtons = new FlowLayoutPanel { Dock = DockStyle.Right, Width = 142, FlowDirection = FlowDirection.LeftToRight, WrapContents = false, BackColor = surface, Padding = new Padding(0, 8, 0, 0) };
        var minimize = WindowButton("—");
        minimize.Click += (_, _) => WindowState = FormWindowState.Minimized;
        var maximize = WindowButton("□");
        maximize.Click += (_, _) => ToggleFullScreen();
        var close = WindowButton("×");
        close.ForeColor = Color.FromArgb(255, 135, 150);
        close.Click += (_, _) => Close();
        windowButtons.Controls.AddRange([minimize, maximize, close]);
        topBar.Controls.Add(topHint);
        topBar.Controls.Add(brand);
        topBar.Controls.Add(windowButtons);
        topBar.MouseDown += DragWindow;
        brand.MouseDown += DragWindow;
        topHint.MouseDown += DragWindow;
        root.Controls.Add(topBar, 0, 0);

        var body = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 1, Padding = new Padding(24, 22, 24, 18), BackColor = background };
        body.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 260));
        body.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        var side = new Panel { Dock = DockStyle.Fill, BackColor = surface, Padding = new Padding(22) };
        var sideLayout = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 1, RowCount = 6, BackColor = surface };
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Absolute, 92));
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Absolute, 1));
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Absolute, 62));
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Absolute, 62));
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        sideLayout.RowStyles.Add(new RowStyle(SizeType.Absolute, 58));
        var sideLogo = new Label { Text = "NULL", Dock = DockStyle.Fill, ForeColor = Color.White, Font = new Font("Segoe UI", 29, FontStyle.Bold), TextAlign = ContentAlignment.BottomLeft };
        var sideLine = new Panel { Dock = DockStyle.Fill, BackColor = accent, Height = 2 };
        var homeLabel = SideItem("◈   Главная");
        var accountLabel = SideItem("◉   Аккаунт");
        var sideSpacer = new Panel { Dock = DockStyle.Fill, BackColor = surface };
        var version = new Label { Text = "BUILD 1.2.0\nFABRIC 1.21.4", Dock = DockStyle.Fill, ForeColor = Color.FromArgb(125, 132, 150), Font = new Font("Segoe UI", 8, FontStyle.Bold), TextAlign = ContentAlignment.BottomLeft };
        sideLayout.Controls.Add(sideLogo, 0, 0);
        sideLayout.Controls.Add(sideLine, 0, 1);
        sideLayout.Controls.Add(homeLabel, 0, 2);
        sideLayout.Controls.Add(accountLabel, 0, 3);
        sideLayout.Controls.Add(sideSpacer, 0, 4);
        sideLayout.Controls.Add(version, 0, 5);
        side.Controls.Add(sideLayout);
        body.Controls.Add(side, 0, 0);

        var content = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 1, RowCount = 4, Padding = new Padding(28, 2, 0, 0), BackColor = background };
        content.RowStyles.Add(new RowStyle(SizeType.Absolute, 76));
        content.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        content.RowStyles.Add(new RowStyle(SizeType.Absolute, 82));
        content.RowStyles.Add(new RowStyle(SizeType.Absolute, 36));
        var heading = new Panel { Dock = DockStyle.Fill, BackColor = background };
        heading.Controls.Add(new Label { Text = "Добро пожаловать", Dock = DockStyle.Top, Height = 38, ForeColor = Color.White, Font = new Font("Segoe UI", 20, FontStyle.Bold) });
        heading.Controls.Add(new Label { Text = "Управляй аккаунтом и запускай свой игровой клиент", Dock = DockStyle.Bottom, Height = 28, ForeColor = Color.FromArgb(145, 151, 168), Font = new Font("Segoe UI", 9.5f) });
        content.Controls.Add(heading, 0, 0);

        tabs.Dock = DockStyle.Fill;
        tabs.Appearance = TabAppearance.Normal;
        tabs.SizeMode = TabSizeMode.Fixed;
        tabs.ItemSize = new Size(150, 38);
        tabs.DrawMode = TabDrawMode.OwnerDrawFixed;
        tabs.DrawItem += DrawTab;
        tabs.Padding = new Point(18, 6);
        tabs.BackColor = surface;
        tabs.TabPages.Add(BuildRegisterTab());
        tabs.TabPages.Add(BuildLoginTab());
        content.Controls.Add(tabs, 0, 1);

        var gameSettings = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 2, RowCount = 2, BackColor = background, Padding = new Padding(0, 12, 0, 0) };
        gameSettings.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        gameSettings.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 130));
        gameSettings.RowStyles.Add(new RowStyle(SizeType.Absolute, 22));
        gameSettings.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        var pathLabel = new Label { Text = "Путь к Legacy Launcher", Dock = DockStyle.Fill, ForeColor = Color.FromArgb(174, 180, 194), Font = new Font("Segoe UI", 8.5f, FontStyle.Bold) };
        launcherPath.Dock = DockStyle.Fill;
        launcherPath.Text = LoadLauncherPath();
        StyleTextBox(launcherPath);
        var browse = MakeButton("ОБЗОР", false);
        browse.Dock = DockStyle.Fill;
        browse.Click += (_, _) => ChooseLegacyLauncher();
        gameSettings.Controls.Add(pathLabel, 0, 0);
        gameSettings.Controls.Add(new Panel { Dock = DockStyle.Fill, BackColor = background }, 1, 0);
        gameSettings.Controls.Add(launcherPath, 0, 1);
        gameSettings.Controls.Add(browse, 1, 1);
        content.Controls.Add(gameSettings, 0, 2);

        status.Dock = DockStyle.Fill;
        status.ForeColor = Color.FromArgb(145, 151, 168);
        status.Font = new Font("Segoe UI", 8.5f);
        status.TextAlign = ContentAlignment.MiddleLeft;
        status.Text = "Аккаунты хранятся локально на этом ПК. Вход Minecraft выполняется отдельно.";
        content.Controls.Add(status, 0, 3);
        body.Controls.Add(content, 1, 0);
        root.Controls.Add(body, 0, 1);

        var footer = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 3, RowCount = 1, Padding = new Padding(284, 12, 24, 12), BackColor = surface };
        footer.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        footer.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        footer.ColumnStyles.Add(new ColumnStyle(SizeType.Absolute, 0));
        var install = MakeButton("ОБНОВИТЬ NULL", false);
        install.Dock = DockStyle.Fill;
        install.Click += (_, _) => InstallMod();
        var play = MakeButton("ЗАПУСТИТЬ ИГРУ  →", true);
        play.Dock = DockStyle.Fill;
        play.Click += (_, _) => LaunchLegacy();
        footer.Controls.Add(install, 0, 0);
        footer.Controls.Add(play, 1, 0);
        root.Controls.Add(footer, 0, 2);

        InstallMod();
    }

    private Button WindowButton(string text) => new()
    {
        Text = text, Width = 40, Height = 34, FlatStyle = FlatStyle.Flat,
        BackColor = surface2, ForeColor = Color.White, Font = new Font("Segoe UI", 12),
        Margin = new Padding(3, 0, 3, 0), Cursor = Cursors.Hand
    };

    private Label SideItem(string text) => new()
    {
        Text = text, Dock = DockStyle.Fill, ForeColor = Color.FromArgb(215, 218, 228),
        Font = new Font("Segoe UI", 10, FontStyle.Bold), TextAlign = ContentAlignment.MiddleLeft
    };

    private void DragWindow(object? sender, MouseEventArgs e)
    {
        if (e.Button != MouseButtons.Left || fullScreen) return;
        ReleaseCapture();
        SendMessage(Handle, 0xA1, 0x2, 0);
    }

    [System.Runtime.InteropServices.DllImport("user32.dll")]
    private static extern bool ReleaseCapture();
    [System.Runtime.InteropServices.DllImport("user32.dll")]
    private static extern nint SendMessage(nint hWnd, int msg, nint wParam, nint lParam);

    private void ToggleFullScreen()
    {
        if (!fullScreen)
        {
            previousBounds = Bounds;
            previousBorderStyle = FormBorderStyle;
            previousWindowState = WindowState;
            FormBorderStyle = FormBorderStyle.None;
            WindowState = FormWindowState.Normal;
            Bounds = Screen.FromControl(this).Bounds;
            fullScreen = true;
        }
        else
        {
            Bounds = previousBounds;
            FormBorderStyle = previousBorderStyle;
            WindowState = previousWindowState;
            fullScreen = false;
        }
    }

    private void DrawTab(object? sender, DrawItemEventArgs e)
    {
        var selected = e.Index == tabs.SelectedIndex;
        using var brush = new SolidBrush(selected ? surface2 : surface);
        e.Graphics.FillRectangle(brush, e.Bounds);
        TextRenderer.DrawText(e.Graphics, tabs.TabPages[e.Index].Text, new Font("Segoe UI", 9, FontStyle.Bold),
            e.Bounds, selected ? Color.White : Color.FromArgb(145, 151, 168), TextFormatFlags.HorizontalCenter | TextFormatFlags.VerticalCenter);
        if (selected) using (var pen = new Pen(accent, 2)) e.Graphics.DrawLine(pen, e.Bounds.Left + 18, e.Bounds.Bottom - 2, e.Bounds.Right - 18, e.Bounds.Bottom - 2);
    }

    private TabPage BuildRegisterTab()
    {
        var page = MakeTab("Регистрация");
        var layout = PageLayout();
        AddField(layout, "ПОЧТА", email);
        AddField(layout, "ЛОГИН  ·  будет виден в NULL", login);
        AddField(layout, "ПАРОЛЬ", password, true);
        AddField(layout, "ПОВТОРИ ПАРОЛЬ", repeatPassword, true);
        var button = MakeButton("СОЗДАТЬ АККАУНТ", true);
        button.Height = 44;
        button.Margin = new Padding(0, 10, 0, 0);
        button.Click += (_, _) => Register();
        layout.Controls.Add(button);
        page.Controls.Add(layout);
        return page;
    }

    private TabPage BuildLoginTab()
    {
        var page = MakeTab("Вход");
        var layout = PageLayout();
        var loginEmail = new TextBox();
        var loginPassword = new TextBox();
        AddField(layout, "ПОЧТА", loginEmail);
        AddField(layout, "ПАРОЛЬ", loginPassword, true);
        var button = MakeButton("ВОЙТИ В NULL", true);
        button.Height = 44;
        button.Margin = new Padding(0, 10, 0, 0);
        button.Click += (_, _) => Login(loginEmail.Text, loginPassword.Text);
        layout.Controls.Add(button);
        page.Controls.Add(layout);
        return page;
    }

    private FlowLayoutPanel PageLayout() => new()
    {
        Dock = DockStyle.Fill, FlowDirection = FlowDirection.TopDown, WrapContents = false,
        AutoScroll = true, BackColor = surface, Padding = new Padding(24, 18, 24, 18)
    };

    private TabPage MakeTab(string title) => new(title)
    {
        BackColor = surface, ForeColor = Color.White, Padding = new Padding(12)
    };

    private void AddField(Control parent, string caption, TextBox box, bool secret = false)
    {
        var group = new Panel { Width = 620, Height = 72, BackColor = surface, Margin = new Padding(0, 0, 0, 7) };
        var label = new Label { Text = caption, Dock = DockStyle.Top, Height = 24, ForeColor = Color.FromArgb(165, 171, 187), Font = new Font("Segoe UI", 8, FontStyle.Bold) };
        box.Dock = DockStyle.Top;
        box.Height = 34;
        box.UseSystemPasswordChar = secret;
        StyleTextBox(box);
        group.Controls.Add(box);
        group.Controls.Add(label);
        parent.Controls.Add(group);
    }

    private void StyleTextBox(TextBox box)
    {
        box.BackColor = surface2;
        box.ForeColor = Color.White;
        box.BorderStyle = BorderStyle.FixedSingle;
        box.Font = new Font("Segoe UI", 10);
        box.Margin = new Padding(0, 3, 0, 0);
    }

    private Button MakeButton(string text, bool primary)
    {
        var button = new Button
        {
            Text = text, Height = 40, FlatStyle = FlatStyle.Flat,
            BackColor = primary ? accent : surface2,
            ForeColor = Color.White, Cursor = Cursors.Hand,
            Font = new Font("Segoe UI", 9, FontStyle.Bold),
            Margin = new Padding(4), Padding = new Padding(12, 0, 12, 0)
        };
        button.FlatAppearance.BorderSize = 0;
        button.FlatAppearance.MouseOverBackColor = primary ? Color.FromArgb(174, 137, 255) : Color.FromArgb(43, 48, 64);
        button.FlatAppearance.MouseDownBackColor = Color.FromArgb(116, 77, 205);
        return button;
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
