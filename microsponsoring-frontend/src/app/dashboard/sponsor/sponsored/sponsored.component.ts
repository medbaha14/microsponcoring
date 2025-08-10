import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgChartsModule, BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import { PaymentService } from '../../../services/payment.service';
import { UserService } from '../../../services/user.service';
import { SponsorService } from '../../../services/sponsor.service';
import { TokenHandler } from '../../../services/token-handler';

@Component({
  selector: 'app-sponsored',
  standalone: true,
  imports: [CommonModule, NgChartsModule],
  templateUrl: './sponsored.component.html',
  styleUrl: './sponsored.component.css'
})
export class SponsoredComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;
  sponsorships: any[] = [];
  sponsorNames: { [sponsorId: string]: string } = {};
  chartType: ChartType = 'bar';
  chartData: any[] = [];
  chartLabels: string[] = [];
  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        labels: {
          color: '#222831',
          font: { size: 14 }
        }
      },
      title: {
        display: false
      },
      tooltip: {
        backgroundColor: '#393E46',
        titleColor: '#DFD0B8',
        bodyColor: '#DFD0B8',
      }
    },
    scales: {
      x: {
        title: {
          display: true,
          text: 'Date',
          color: '#222831',
          font: { size: 20, weight: 'bold' }
        },
        ticks: {
          color: '#222831',
        },
        grid: {
          color: '#393E46',
        }
      },
      y: {
        title: {
          display: true,
          text: 'Amount',
          color: '#222831',
          font: { size: 20, weight: 'bold' }
        },
        ticks: {
          color: '#222831',
        },
        grid: {
          color: '#393E46',
        }
      }
    }
  };

  constructor(
    private paymentService: PaymentService,
    private userService: UserService,
    private sponsorService: SponsorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      // Get sponsor data for the current user
      this.sponsorService.getByUserId(user.userId).subscribe((sponsor: any) => {
        if (sponsor && sponsor.sponsorId) {
          // Get invoices for this sponsor
          this.paymentService.getInvoicesBySponsor(sponsor.sponsorId).subscribe((invoices: any[]) => {
            console.log('Fetched sponsor invoices:', invoices);
            this.sponsorships = invoices;

            // Find the latest sponsorship date
            const latest = invoices.reduce((max, s) => {
              const d = new Date(s.createdAt);
              return d > max ? d : max;
            }, new Date(invoices[0]?.createdAt || Date.now()));

            // Get Monday of that week
            const monday = new Date(latest);
            monday.setDate(latest.getDate() - ((latest.getDay() + 6) % 7));

            // Build 7 days (Mon-Sun)
            const weekDates: string[] = [];
            for (let i = 0; i < 7; i++) {
              const d = new Date(monday);
              d.setDate(monday.getDate() + i);
              weekDates.push(d.toLocaleDateString());
            }

            // Group by date and sum amounts
            const grouped: { [date: string]: number } = {};
            invoices.forEach(s => {
              const date = new Date(s.createdAt).toLocaleDateString();
              if (!grouped[date]) grouped[date] = 0;
              grouped[date] += s.amount;
            });

            this.chartLabels = weekDates;
            this.chartData = [{
              data: weekDates.map(date => grouped[date] || 0),
              label: 'Investment Amount',
              fill: true,
              tension: 0.4,
              borderColor: getComputedStyle(document.documentElement).getPropertyValue('--color-medium-teal').trim() || '#393E46',
              backgroundColor: getComputedStyle(document.documentElement).getPropertyValue('--color-light-teal').trim() || '#948979',
              pointBackgroundColor: getComputedStyle(document.documentElement).getPropertyValue('--color-dark-teal').trim() || '#222831',
              pointBorderColor: getComputedStyle(document.documentElement).getPropertyValue('--color-dark-teal').trim() || '#222831',
            }];

            // Get organization names from the invoice data
            invoices.forEach(invoice => {
              if (invoice.company && invoice.company.companyId) {
                // Use the company's activity type or user's full name if available
                const companyName = invoice.company.activityType || 
                                  (invoice.company.user ? invoice.company.user.fullName : 'Organization');
                this.sponsorNames[invoice.company.companyId] = companyName;
              }
            });
            this.updateChartColors();
          });
        }
      });
    }
    // Observe theme changes on both <html> and <body>
    const observer = new MutationObserver(() => {
      console.log('Theme class changed!');
      this.updateChartColors();
    });
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
    observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
  }

  updateChartColors() {
    // Use dark mode if either <html> or <body> has the class
    const dark = document.documentElement.classList.contains('dark-mode') || document.body.classList.contains('dark-mode');
    console.log('dark:', dark);
    console.log('--color-cream:', getComputedStyle(document.documentElement).getPropertyValue('--color-cream'));
    console.log('--color-dark-teal:', getComputedStyle(document.documentElement).getPropertyValue('--color-dark-teal'));
    const axisColor = getComputedStyle(document.documentElement).getPropertyValue(dark ? '--color-cream' : '--color-dark-teal').trim() || (dark ? '#DFD0B8' : '#222831');
    const gridColor = getComputedStyle(document.documentElement).getPropertyValue('--color-medium-teal').trim() || '#393E46';
    const barColor = getComputedStyle(document.documentElement).getPropertyValue('--color-light-teal').trim() || '#948979';
    const borderColor = getComputedStyle(document.documentElement).getPropertyValue('--color-medium-teal').trim() || '#393E46';
    const pointColor = getComputedStyle(document.documentElement).getPropertyValue('--color-dark-teal').trim() || '#222831';
    const cream = getComputedStyle(document.documentElement).getPropertyValue('--color-cream').trim() || '#DFD0B8';

    // Update dataset colors
    if (this.chartData && this.chartData[0]) {
      this.chartData[0].backgroundColor = barColor;
      this.chartData[0].borderColor = borderColor;
      this.chartData[0].pointBackgroundColor = pointColor;
      this.chartData[0].pointBorderColor = pointColor;
    }
    // Update axes
    if (this.chartOptions && this.chartOptions.scales) {
      const x = (this.chartOptions.scales as any)['x'];
      const y = (this.chartOptions.scales as any)['y'];
      if (x) {
        if (x['title']) x['title'].color = axisColor;
        if (x['ticks']) x['ticks'].color = axisColor;
        if (x['grid']) x['grid'].color = gridColor;
        if (x['title']) x['title'].font = { size: 20, weight: 'bold' };
      }
      if (y) {
        if (y['title']) y['title'].color = axisColor;
        if (y['ticks']) y['ticks'].color = axisColor;
        if (y['grid']) y['grid'].color = gridColor;
        if (y['title']) y['title'].font = { size: 20, weight: 'bold' };
      }
    }
    // Update legend and tooltip
    if (this.chartOptions && this.chartOptions.plugins) {
      const legend = this.chartOptions.plugins.legend;
      const tooltip = this.chartOptions.plugins.tooltip;
      const legendLabels = (legend && legend.labels) ? (legend.labels as any) : null;
      if (legend && legend.labels && legendLabels) legendLabels.color = axisColor;
      if (tooltip) {
        (tooltip as any).backgroundColor = gridColor;
        (tooltip as any).titleColor = cream;
        (tooltip as any).bodyColor = cream;
      }
    }
    if (this.chart) {
      this.chart.update();
    }
    this.chartOptions = { ...this.chartOptions };
    this.cdr.detectChanges();
  }

  downloadPdf(sponsorship: any) {
    const backendUrl = `http://localhost:8080/api/invoices/${sponsorship.invoiceId}/pdf`;
    window.open(backendUrl, '_blank');
  }

  getOrganizationName(s: any): string {
    if (s.company && s.company.companyId) {
      return this.sponsorNames[s.company.companyId] || s.company.activityType || 'Organization';
    }
    return 'N/A';
  }
}