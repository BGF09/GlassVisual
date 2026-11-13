using System.Reflection;

namespace NullInstaller;

internal static class Program
{
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();
        Application.Run(new InstallerForm());
    }
}

internal sealed class InstallerForm : Form
{
    private readonly TextBox folder = new();
    private readonly Label status = new();

    public InstallerForm()
    {
        Text = "NULL Installer";
        Width = 560; Height = 300;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        BackColor = Color.FromArgb(16, 18, 24);
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10);
        var title = new Label { Text = "NULL  /  INSTALLER", Font = new Font("Segoe UI", 20, FontStyle.Bold), ForeColor = Color.FromArgb(130, 255, 190), AutoSize = true, Left = 26, Top = 22 };
        var hint = new Label { Text = "Установка клиента для Minecraft 1.21.4 + Fabric", AutoSize = true, Left = 28, Top = 70, ForeColor = Color.Gainsboro };
        folder.SetBounds(28, 122, 390, 32);
        folder.Text = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");
        var browse = MakeButton("Обзор…", 428, 121, 95, 34);
        browse.Click += (_, _) => { using var d = new FolderBrowserDialog(); if (d.ShowDialog() == DialogResult.OK) folder.Text = d.SelectedPath; };
        var install = MakeButton("УСТАНОВИТЬ NULL", 28, 174, 495, 42);
        install.BackColor = Color.FromArgb(45, 125, 90);
        install.Click += (_, _) => Install();
        status.SetBounds(28, 232, 495, 28);
        status.ForeColor = Color.LightGray;
        Controls.AddRange([title, hint, folder, browse, install, status]);
    }

    private Button MakeButton(string text, int x, int y, int w, int h)
    {
        var b = new Button { Text = text, Left = x, Top = y, Width = w, Height = h, FlatStyle = FlatStyle.Flat, BackColor = Color.FromArgb(42, 46, 56), ForeColor = Color.White, Cursor = Cursors.Hand };
        b.FlatAppearance.BorderSize = 0;
        return b;
    }

    private void Install()
    {
        try
        {
            var root = folder.Text.Trim();
            if (string.IsNullOrWhiteSpace(root)) throw new InvalidOperationException("Укажи папку .minecraft.");
            var mods = Path.Combine(root, "mods");
            Directory.CreateDirectory(mods);
            using var source = Assembly.GetExecutingAssembly().GetManifestResourceStream("NULL.jar")
                ?? throw new FileNotFoundException("В сборку не встроен файл NULL.jar.");
            var destination = Path.Combine(mods, "NULL-Visual-1.0.0.jar");
            using var target = File.Create(destination);
            source.CopyTo(target);
            status.Text = "Готово! NULL установлен в папку mods.";
            MessageBox.Show("NULL установлен. Запусти профиль Fabric для Minecraft 1.21.4.", "NULL Installer", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            status.Text = "Ошибка установки.";
            MessageBox.Show(ex.Message, "NULL Installer", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
