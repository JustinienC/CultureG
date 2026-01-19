#!/usr/bin/env python3
"""
Script pour convertir les fichiers Markdown en PDF
"""

import os
import sys
from pathlib import Path

try:
    import markdown
    from weasyprint import HTML, CSS
    from weasyprint.text.fonts import FontConfiguration
except ImportError:
    print("Installation des dépendances nécessaires...")
    print("Exécutez: pip install markdown weasyprint")
    sys.exit(1)

def convert_md_to_pdf(md_file, pdf_file):
    """Convertit un fichier Markdown en PDF"""
    try:
        # Lire le fichier Markdown
        with open(md_file, 'r', encoding='utf-8') as f:
            md_content = f.read()
        
        # Convertir Markdown en HTML
        html_content = markdown.markdown(
            md_content,
            extensions=['extra', 'codehilite', 'tables', 'fenced_code']
        )
        
        # Ajouter un style CSS pour améliorer l'apparence
        css_style = """
        <style>
            @page {
                size: A4;
                margin: 2cm;
            }
            body {
                font-family: 'Segoe UI', Arial, sans-serif;
                font-size: 11pt;
                line-height: 1.6;
                color: #333;
            }
            h1 {
                color: #2c3e50;
                border-bottom: 3px solid #3498db;
                padding-bottom: 10px;
                margin-top: 30px;
            }
            h2 {
                color: #34495e;
                border-bottom: 2px solid #95a5a6;
                padding-bottom: 5px;
                margin-top: 25px;
            }
            h3 {
                color: #7f8c8d;
                margin-top: 20px;
            }
            code {
                background-color: #f4f4f4;
                padding: 2px 6px;
                border-radius: 3px;
                font-family: 'Consolas', 'Monaco', monospace;
                font-size: 10pt;
            }
            pre {
                background-color: #f4f4f4;
                padding: 15px;
                border-radius: 5px;
                overflow-x: auto;
                border-left: 4px solid #3498db;
            }
            pre code {
                background-color: transparent;
                padding: 0;
            }
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 20px 0;
            }
            th, td {
                border: 1px solid #ddd;
                padding: 12px;
                text-align: left;
            }
            th {
                background-color: #3498db;
                color: white;
                font-weight: bold;
            }
            tr:nth-child(even) {
                background-color: #f2f2f2;
            }
            blockquote {
                border-left: 4px solid #3498db;
                margin: 20px 0;
                padding-left: 20px;
                color: #555;
                font-style: italic;
            }
            ul, ol {
                margin: 15px 0;
                padding-left: 30px;
            }
            li {
                margin: 8px 0;
            }
            strong {
                color: #2c3e50;
            }
            a {
                color: #3498db;
                text-decoration: none;
            }
        </style>
        """
        
        # Créer le HTML complet
        full_html = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            {css_style}
        </head>
        <body>
            {html_content}
        </body>
        </html>
        """
        
        # Convertir HTML en PDF
        HTML(string=full_html).write_pdf(pdf_file)
        
        print(f"✅ Converti: {md_file} → {pdf_file}")
        return True
        
    except Exception as e:
        print(f"❌ Erreur lors de la conversion de {md_file}: {e}")
        return False

def main():
    """Fonction principale"""
    # Fichiers à convertir
    md_files = [
        "PERSONNE_3_APPLICATION_ANDROID.md",
        "PERSONNE_4_SERVEUR_FLASK.md",
        "PERSONNE_5_LOGIQUE_METIER.md"
    ]
    
    converted = 0
    failed = 0
    
    for md_file in md_files:
        if not os.path.exists(md_file):
            print(f"⚠️  Fichier non trouvé: {md_file}")
            failed += 1
            continue
        
        pdf_file = md_file.replace('.md', '.pdf')
        
        if convert_md_to_pdf(md_file, pdf_file):
            converted += 1
        else:
            failed += 1
    
    print(f"\n📊 Résumé: {converted} converti(s), {failed} échec(s)")
    
    if failed > 0:
        print("\n💡 Si vous avez des erreurs, installez les dépendances:")
        print("   pip install markdown weasyprint")

if __name__ == "__main__":
    main()
